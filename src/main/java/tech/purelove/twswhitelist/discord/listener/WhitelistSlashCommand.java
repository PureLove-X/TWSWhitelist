package tech.purelove.twswhitelist.discord.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.twswhitelist.config.WhitelistConfig;
import tech.purelove.twswhitelist.discord.util.WhitelistLogger;
import tech.purelove.twswhitelist.whitelist.WhitelistService;

import java.util.Objects;

public class WhitelistSlashCommand extends ListenerAdapter {

    private final JavaPlugin plugin;
    private final WhitelistConfig config;

    public WhitelistSlashCommand(JavaPlugin plugin, WhitelistConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getName().equals("devlist")) return;
        if (event.getGuild() == null) return;

        Member staff = event.getMember();
        if (staff == null || staff.getRoles().stream()
                .noneMatch(r -> r.getId().equals(config.roles().staffRoleId()))) {

            event.reply("❌ Staff only command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String username = Objects.requireNonNull(event.getOption("username")).getAsString().trim();
        event.deferReply(true).queue();

        switch (Objects.requireNonNull(event.getSubcommandName())) {

            case "add" -> {
                WhitelistService.whitelist(username)
                        .thenAccept(result -> {

                            switch (result) {
                                case SUCCESS -> {
                                    event.getHook()
                                        .sendMessage("✅ Whitelisted `" + username + "`")
                                        .queue();
                                    WhitelistLogger.log(
                                            event.getJDA(),
                                            config,
                                            "ADD",
                                            staff,
                                            null,
                                            null,
                                            username,
                                            "Slash Command"
                                    );

                                }

                                case FAILED -> event.getHook()
                                        .sendMessage("❌ Username doesn't exist")
                                        .queue();

                                case ERROR -> event.getHook()
                                        .sendMessage("❌ Internal error.")
                                        .queue();
                            }
                        });
            }

            case "remove" -> {
                Bukkit.getScheduler().runTask(
                        plugin,
                        () -> Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                "whitelist remove " + username
                        )
                );

                event.getHook()
                        .sendMessage("🗑️ Removed `" + username + "` from the whitelist.")
                        .queue();

                WhitelistLogger.log(
                        event.getJDA(),
                        config,
                        "REMOVE",
                        staff,
                        null,
                        null,
                        username,
                        "Slash Command"
                );

            }
        }
    }
}

