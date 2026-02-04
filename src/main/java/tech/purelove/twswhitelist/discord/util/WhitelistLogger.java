package tech.purelove.twswhitelist.discord.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jspecify.annotations.Nullable;
import tech.purelove.twswhitelist.config.WhitelistConfig;
import tech.purelove.twswhitelist.util.LogUtils;

import java.time.Instant;
import java.util.List;

public final class WhitelistLogger {

    private WhitelistLogger() {}
    //TODO: Make Whitelist logs configurable
    //ISSUE: Currently when I try to make them apart of the config it causes the plugin to timeout on discords end.
    // In theory it's doable but not priority unless I decide to publish this on an actual platform.
    public static void log(
            JDA jda,
            WhitelistConfig config,
            String action,
            Member staff,
            @Nullable Member applicant,
            @Nullable String applicantID,
            @Nullable String username,
            String source
    ) {
        // ---- CONSOLE LOG ----
        LogUtils.info(
                "[Whitelist] %s | %s | by %s | source=%s"
                        .formatted(
                                action,
                                username,
                                staff.getEffectiveName(),
                                source
                        )
        );

        // ---- DISCORD LOG ----
        GuildMessageChannel channel = jda.getChannelById(
                GuildMessageChannel.class,
                config.channels().whitelistLogChannelId()
        );

        if (channel == null) return;

        String staffDisplay =
                "<@" + staff.getId() + ">";

        if (source.equals("Rewhitelist Approval")) {
            String applicantDisplay =
                    applicantID != null
                            ? "<@" + applicantID + ">"
                            : "Unknown";

            channel.sendMessage(
                            """
                            🧾 **Whitelist Action**
                            **Action:** %s
                            **Discord Username:** %s
                            **Minecraft Username:** `%s`
                            **Staff:** %s
                            **Source:** %s
                            **Time:** <t:%d:F>
                            """
                                    .formatted(
                                            action,
                                            applicantDisplay,
                                            username,
                                            staffDisplay,
                                            source,
                                            Instant.now().getEpochSecond()
                                    )
                    ).setAllowedMentions(List.of()) // 🔒 prevents ping
                    .queue();
            return;
        }
        String applicantDisplay =
                applicant != null
                        ? "<@" + applicant.getId() + ">"
                        : "Unknown";
        channel.sendMessage(
                """
                🧾 **Whitelist Action**
                **Action:** %s
                **Discord Username:** %s
                **Minecraft Username:** `%s`
                **Staff:** %s
                **Source:** %s
                **Time:** <t:%d:F>
                """
                        .formatted(
                                action,
                                applicantDisplay,
                                username,
                                staffDisplay,
                                source,
                                Instant.now().getEpochSecond()
                        )
        ).setAllowedMentions(List.of()) // 🔒 prevents ping
                .queue();
    }
}
