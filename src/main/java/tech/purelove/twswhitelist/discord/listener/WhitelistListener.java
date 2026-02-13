package tech.purelove.twswhitelist.discord.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import tech.purelove.twswhitelist.config.messages.player.DeniedReason;
import tech.purelove.twswhitelist.config.WhitelistConfig;
import tech.purelove.twswhitelist.discord.ids.DashboardIds;
import tech.purelove.twswhitelist.discord.ids.ModalIds;
import tech.purelove.twswhitelist.discord.service.DashboardBuilder;
import tech.purelove.twswhitelist.discord.service.DenySelectBuilder;
import tech.purelove.twswhitelist.discord.service.RoleService;
import tech.purelove.twswhitelist.discord.util.*;
import tech.purelove.twswhitelist.whitelist.WhitelistResult;
import tech.purelove.twswhitelist.whitelist.WhitelistService;
import tech.purelove.twswhitelist.discord.util.MessageFormatter;
import tech.purelove.twswhitelist.discord.util.WhitelistLogger;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhitelistListener extends ListenerAdapter {
//TODO: Split Listener class into more of dispatch system instead of one class
    //NOTE: currently it's just one class for the sake of time
    // It as is shouldn't cause any unneeded server strain but in the long run if I split it
    // into more of a module type system it would be easier to maintain.
    private final WhitelistConfig config;

    public WhitelistListener(WhitelistConfig config) {
        this.config = config;
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {

        // Must be the configured server
        if (!event.getGuild().getId().equals(config.bot().serverId())) {
            return;
        }

        // Must be categorizable (can belong to a category)
        if (!(event.getChannel() instanceof ICategorizableChannel categorizable)) {
            return;
        }

        // Must be under the application category
        if (categorizable.getParentCategory() == null ||
                !categorizable.getParentCategory().getId()
                        .equals(config.channels().applicationCategoryId())) {
            return;
        }

        // Must be able to send messages
        if (!(event.getChannel() instanceof GuildMessageChannel messageChannel)) {
            return;
        }

        // Delay dashboard so ticket tool message appears first
        CompletableFuture.runAsync(
                () -> DashboardBuilder.send(messageChannel),
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
        );
    }
    private @Nullable String resolveMinecraftUsername(
            @Nullable ModalInteractionEvent modalEvent,
            @Nullable GuildMessageChannel channel
    ) {
        // 1️⃣ Modal value is authoritative (approve / rewhitelist)
        if (modalEvent != null) {
            var value = modalEvent.getValue(ModalIds.MC_USERNAME);
            if (value != null) {
                return value.getAsString().trim();
            }
        }

        // 2️⃣ Fall back to application context (deny / more-info)
        if (channel != null) {
            return resolveIgnFromTicket(channel);
        }

        return null;
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        // Must be in a guild
        Guild guild = event.getGuild();
        if (guild == null) return;

        // Must be the configured server
        if (!guild.getId().equals(config.bot().serverId())) return;

        // Rewhitelist channel
        if (event.getComponentId().equals(DashboardIds.REWHITELIST_CONFIRM)) {

            if (!Permissions.requireStaff(event, config)) return;

            String detectedIgn = null;

            List<MessageEmbed> embeds = event.getMessage().getEmbeds();
            if (!embeds.isEmpty()) {
                MessageEmbed embed = embeds.getFirst();

                String desc = embed.getDescription();
                if (desc != null) {
                    for (String line : desc.split("\n")) {
                        if (line.startsWith("**IGN:**")) {
                            detectedIgn = line
                                    .replace("**IGN:**", "")
                                    .replace("`", "")
                                    .trim();
                            break;
                        }
                    }
                }
            }
            TextInput.Builder input = TextInput.create(
                            ModalIds.MC_USERNAME,
                            "Minecraft Username",
                            TextInputStyle.SHORT
                    )
                    .setRequired(true)
                    .setMinLength(3)
                    .setMaxLength(16);

            if (detectedIgn != null) {
                input.setValue(detectedIgn);
            }

            event.replyModal(
                    Modal.create(
                            ModalIds.REWHITELIST_MODAL + ":" + event.getMessageId(),
                            "Confirm Rewhitelist"
                    ).addComponents(
                            ActionRow.of(input.build())
                    ).build()
            ).queue();

            return;
        }

        //Ticket Tool whitelist apps

        if (!(event.getChannel() instanceof ICategorizableChannel channel)) return;

        if (channel.getParentCategory() == null ||
                !channel.getParentCategory().getId()
                        .equals(config.channels().applicationCategoryId())) {
            return;
        }

        if (!Permissions.requireStaff(event, config)) return;

        switch (event.getComponentId()) {

            case DashboardIds.APPROVE -> {

                GuildMessageChannel msgChannel = event.getChannel().asGuildMessageChannel();
                // Try to detect IGN from ticket
                String detectedIgn = resolveIgnFromTicket(msgChannel);

                TextInput.Builder usernameBuilder = TextInput.create(
                                ModalIds.MC_USERNAME,
                                "Minecraft Username",
                                TextInputStyle.SHORT
                        )
                        .setRequired(true)
                        .setMinLength(3)
                        .setMaxLength(16);

                // Prefill if found
                if (detectedIgn != null) {
                    usernameBuilder.setValue(detectedIgn);
                }

                TextInput username = usernameBuilder.build();

                Modal modal = Modal.create(
                                ModalIds.APPROVE_MODAL,
                                "Approve Whitelist"
                        )
                        .addComponents(ActionRow.of(username))
                        .build();

                event.replyModal(modal).queue();
            }

            case DashboardIds.DENY -> {
                event.reply(
                                formatMessage(
                                        "Select a reason for denial:",
                                        event.getMember()
                                )
                        )
                        .addActionRow(DenySelectBuilder.build(config))
                        .setEphemeral(true)
                        .queue();
            }

            case DashboardIds.MORE_INFO -> {

                event.deferEdit().queue(); // ✅ ACK the button

                GuildMessageChannel msgChannel = event.getChannel().asGuildMessageChannel();

                Member applicant = resolveApplicant(msgChannel);
                if (applicant == null) {
                    event.getHook()
                            .sendMessage(formatMessage(
                                    config.messages().staff().applicantError(),
                                    event.getMember()
                            ))
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                msgChannel.sendMessage(
                        formatMessage(
                                config.messages().player().moreInfo(),
                                applicant
                        )
                ).queue();

                String mcUsername = resolveMinecraftUsername(null, msgChannel);
                Member staff = event.getMember() != null
                        ? event.getMember()
                        : msgChannel.getGuild().getSelfMember();

                WhitelistLogger.log(
                        event.getJDA(),
                        config,
                        "MORE-INFO",
                        staff,
                        applicant,
                        null,
                        mcUsername,
                        "Application Review"
                );
        }

            default -> {
                // ignore unknown buttons
            }
        }
    }

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {

        event.deferReply(true).queue(); // ✅ ONLY HERE

        if (!Permissions.requireStaff(event, config)) {
            event.getHook()
                    .sendMessage(config.messages().staff().applicantError())
                    .queue();
            return;
        }

        String modalId = event.getModalId();

        if (modalId.startsWith(ModalIds.REWHITELIST_MODAL)) {
            handleRewhitelistModal(event);
            return;
        }

        if (modalId.equals(ModalIds.APPROVE_MODAL)) {
            handleApproveModal(event);
        }
    }


    private @Nullable String getApplicantDiscordIdFromEmbed(Message msg) {
        if (msg.getEmbeds().isEmpty()) return null;

        MessageEmbed embed = msg.getEmbeds().getFirst();
        String desc = embed.getDescription();
        if (desc == null) return null;

        for (String line : desc.split("\n")) {
            if (line.startsWith("**Discord ID:**")) {
                return line
                        .replace("**Discord ID:**", "")
                        .replace("`", "")
                        .trim();
            }
        }
        return null;
    }


    private @Nullable String getApplicantNameFromEmbed(Message msg) {
        if (msg.getEmbeds().isEmpty()) return null;

        MessageEmbed embed = msg.getEmbeds().getFirst();
        MessageEmbed.AuthorInfo author = embed.getAuthor();

        return author != null ? author.getName() : null;
    }


    private void handleRewhitelistModal(ModalInteractionEvent event) {

        String modalId = event.getModalId();
        String messageId;

        if (modalId.contains(":")) {
            messageId = modalId.substring(modalId.indexOf(":") + 1);
        } else {
            messageId = null;
        }

        String username = Objects.requireNonNull(
                event.getValue(ModalIds.MC_USERNAME)
        ).getAsString().trim();

        WhitelistService.whitelist(username)
                .orTimeout(10, TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {
                        event.getHook()
                                .sendMessage(
                                        formatMessage(
                                                config.messages().staff().approve().timeout(),
                                                null,
                                                username
                                        )
                                )
                                .queue();
                        return;
                    }

                    if (result != WhitelistResult.SUCCESS) {
                        event.getHook()
                                .sendMessage(
                                        formatMessage(
                                                config.messages().staff().approve().failed(),
                                                null,
                                                username
                                        )
                                )
                                .queue();
                        return;
                    }
                    // success
                    if (messageId != null) {
                        event.getChannel()
                                .retrieveMessageById(messageId)
                                .queue(msg -> {

                                    String applicantId = getApplicantDiscordIdFromEmbed(msg);
                                    String applicantName = getApplicantNameFromEmbed(msg);
                                    event.getHook()
                                            .sendMessage(
                                                    MessageFormatter.format(
                                                            String.join("\n", config.messages().staff().approve().success()),
                                                            Map.of(
                                                                    "applicant", applicantName != null ? applicantName : "Unknown",
                                                                    "username", username
                                                            )
                                                    )
                                            )
                                            .queue();

                                    MessageEmbed oldEmbed = msg.getEmbeds().isEmpty()
                                            ? null
                                            : msg.getEmbeds().getFirst();

                                    if (oldEmbed == null) return;

                                    EmbedBuilder updated = new EmbedBuilder(oldEmbed)
                                            .setColor(0x2ecc71)
                                            .addField("Status", "✅ Whitelisted", false);

                                    msg.editMessageEmbeds(updated.build())
                                            .setActionRow(
                                                    Button.success(
                                                            "rewhitelist_done",
                                                            "Whitelisted"
                                                    ).asDisabled()
                                            )
                                            .queue();
                                    Member staff = event.getMember() != null
                                            ? event.getMember()
                                            : msg.getGuild().getSelfMember();

                                    WhitelistLogger.log(
                                            event.getJDA(),
                                            config,
                                            "ADD",
                                            staff,
                                            null,
                                            applicantId,
                                            username,
                                            "Rewhitelist Approval"
                                    );
                                });
                    }
                });

    }

    private void handleApproveModal(ModalInteractionEvent event) {

        if (!(event.getChannel() instanceof GuildMessageChannel channel)) {
            event.getHook()
                    .sendMessage("❌ Invalid channel type.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        //Get the person who make the application
        Member applicant = resolveApplicant(channel);
        if (applicant == null) {
            event.getHook()
                    .sendMessage(
                    formatMessage(
                            config.messages().staff().applicantError(),
                            event.getMember()
                    )
            ).setEphemeral(true).queue();
            return;
        }
        //prefill mc username
        String mcUsername = Objects.requireNonNull(
                event.getValue(ModalIds.MC_USERNAME)
        ).getAsString().trim();

        WhitelistService.whitelist(mcUsername)
                .thenAccept(result -> {

                    switch (result) {

                        case SUCCESS -> {
                            RoleService.applyApproved(applicant, config);
                            Member staff = event.getMember() != null
                                    ? event.getMember()
                                    : channel.getGuild().getSelfMember();

                            WhitelistLogger.log(
                                    event.getJDA(),
                                    config,
                                    "ADD",
                                    staff,
                                    applicant,
                                    null,
                                    mcUsername,
                                    "Application Approval"
                            );

                            channel.sendMessage(
                                    formatMessage(
                                            config.messages().player().approved(),
                                            applicant,
                                            mcUsername
                                    )
                            ).queue();

                            event.getHook()
                                    .sendMessage(
                                    formatMessage(
                                            config.messages().staff().approve().success(),
                                            applicant,
                                            mcUsername
                                    )
                            ).setEphemeral(true).queue();
                        }

                        case FAILED -> event.getHook()
                                .sendMessage(
                                formatMessage(
                                        config.messages().staff().approve().failed(),
                                        applicant,
                                        mcUsername
                                )
                        ).setEphemeral(true).queue();


                        case ERROR -> event.getHook()
                                .sendMessage(
                                formatMessage(
                                        config.messages().staff().approve().error(),
                                        applicant,
                                        mcUsername
                                )
                        ).setEphemeral(true).queue();
                    }
                });
    }



    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        if (!event.getComponentId().equals("deny_reason")) return;
        if (!Permissions.requireStaff(event, config)) return;
        if (event.getGuild() == null) return;

        event.deferEdit().queue();

        Member applicant = resolveApplicant(
                event.getChannel().asGuildMessageChannel()
        );
        if (applicant == null) {
            event.getHook()
                    .sendMessage(
                            formatMessage(
                                    config.messages().staff().applicantError(),
                                    event.getMember()
                            )
                    )
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String selectedLabel = event.getValues().getFirst();

        DeniedReason reason = config.messages().player().deniedReasons().stream()
                .filter(r -> r.label().equals(selectedLabel))
                .findFirst()
                .orElse(null);

        if (reason == null) {
            event.getHook()
                    .sendMessage(
                            //TODO: Make message configurable
                            formatMessage("❌ Invalid denial reason selected.", applicant)
                    )
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Player-facing denial
        RoleService.applyDenied(applicant, config);
        event.getChannel()
                .asGuildMessageChannel()
                .sendMessage(
                        formatMessage(
                                reason.message(),
                                applicant
                        )
                )
                .queue();
        // LOG DENIAL
        String mcUsername = resolveMinecraftUsername(
                null,
                event.getChannel().asGuildMessageChannel()
        );
        Member staff = event.getMember() != null
                ? event.getMember()
                : event.getGuild().getSelfMember();
        WhitelistLogger.log(
                event.getJDA(),
                config,
                "DENIED: " + reason.label(),
                staff,
                applicant,
                null,
                mcUsername,
                "Application Denial"
        );

        // Staff confirmation
        event.getHook()
                .sendMessage(
                        formatMessage("❌ Application denied.", applicant)
                )
                .setEphemeral(true)
                .queue();

    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;

        if (!event.getChannel().getId()
                .equals(config.channels().rewhitelistLogChannelId())) {
            return;
        }

        String ign = event.getMessage().getContentRaw().trim();
        if (!ign.matches("^[A-Za-z0-9_]{3,16}$")) return;

        long requestedAt = event.getMessage()
                .getTimeCreated()
                .toEpochSecond();

        Member requester = event.getMember();
        TextChannel channel = event.getChannel().asTextChannel();

        assert requester != null;
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(
                        requester.getEffectiveName(),
                        null,
                        requester.getEffectiveAvatarUrl()
                )
                .setDescription(
                        "**IGN:** `" + ign + "`\n" +
                                "**Requested by:** " + requester.getAsMention() + "\n" +
                                "**Discord ID:** `" + requester.getId() + "`"
                )
                .setFooter("Requested")
                .setTimestamp(Instant.ofEpochSecond(requestedAt));

        channel.sendMessageEmbeds(embed.build())
                .setAllowedMentions(List.of())
                .addActionRow(
                        Button.primary(
                                DashboardIds.REWHITELIST_CONFIRM,
                                "Confirm Rewhitelist"
                        )
                )
                .queue();



        event.getMessage().delete().queue();
    }




    //TODO: Move Resolve applicant and resolveIGN to a helper class
    //NOTE: Written in here because when I tried to pass the needed dependencies from this class to that one
    // it would some how would manage to not pick them up again.
    // I have had this problem before with another plugin with JDA but it was a struggle to get that fixed.
    // For the sake of time I've just put them in the one class
    private Member resolveApplicant(GuildMessageChannel channel) {

        Message first = channel.getHistoryFromBeginning(1)
                .complete()
                .getRetrievedHistory()
                .getFirst();

        return first.getMentions().getUsers().stream()
                .map(u -> {
                    try {
                        return channel.getGuild().retrieveMember(u).complete();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> {
                    return null;
                });
    }

    private String resolveIgnFromTicket(GuildMessageChannel channel) {

        List<Message> messages = channel.getHistory()
                .retrievePast(10)
                .complete();

        for (Message msg : messages) {
            for (MessageEmbed embed : msg.getEmbeds()) {

                String desc = embed.getDescription();
                if (desc == null) continue;

                // Look for ```username``` blocks
                Matcher matcher = Pattern.compile("```\\s*([A-Za-z0-9_]{3,16})\\s*```")
                        .matcher(desc);

                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        return null;
    }


    //TODO: Move all Message formatting to MessageFormatter
    // will be done when I rewrite the plugin to include more features
    // currently it's more work to move it than it is worth it.
    private String formatMessage(
            String template,
            Member applicant,
            String username
    ) {
        if (template == null) {
            return "";
        }

        String resolved = resolveChannelMentions(template);

        return MessageFormatter.format(
                resolved,
                Map.of(
                        "mention", applicant != null ? applicant.getAsMention() : "",
                        "applicant", applicant != null ? applicant.getEffectiveName() : "",
                        "username", username != null ? username : ""
                )
        );
    }

    private String resolveChannelMentions(String input) {
        if (input == null) return "";

        return input.replaceAll(
                "\\{channel:(\\d+)}",
                "<#$1>"
        );
    }
    private String formatMessage(
            String template,
            Member applicant
    ) {
        return formatMessage(template, applicant, null);
    }

    private String formatMessage(String template) {
        return MessageFormatter.format(template, Map.of());
    }



}