package tech.purelove.twswhitelist.config;

import org.bukkit.configuration.file.FileConfiguration;
import tech.purelove.twswhitelist.TWSWhitelistPlugin;
import tech.purelove.twswhitelist.config.bot.BotConfig;
import tech.purelove.twswhitelist.config.channels.ChannelConfig;
import tech.purelove.twswhitelist.config.messages.MessagesConfig;
import tech.purelove.twswhitelist.config.messages.player.DeniedReason;
import tech.purelove.twswhitelist.config.messages.staff.ApproveMessages;
import tech.purelove.twswhitelist.config.messages.logs.LogMessages;
import tech.purelove.twswhitelist.config.messages.player.PlayerMessages;
import tech.purelove.twswhitelist.config.messages.staff.StaffMessages;
import tech.purelove.twswhitelist.config.roles.RoleConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigLoader {

    private ConfigLoader() {}

    private static String join(FileConfiguration c, String path) {
        return String.join("\n", c.getStringList(path));
    }
    private static String str(FileConfiguration c, String path) {
        return c.getString(path);
    }

    public static WhitelistConfig load(TWSWhitelistPlugin plugin) {
        FileConfiguration c = plugin.getConfig();

        BotConfig bot = new BotConfig(
                str(c,"Bot.token"),
                str(c,"Bot.server_id")
        );

        ChannelConfig channels = new ChannelConfig(
                str(c,"Channels.application_category_id"),
                str(c,"Channels.whitelist_log_channel_id"),
                str(c,"Channels.rewhitelist_channel_id")
        );

        RoleConfig roles = new RoleConfig(
                str(c,"Roles.staff_role_id"),
                str(c,"Roles.approved_role_id"),
                str(c,"Roles.denied_role_id"),
                str(c,"Roles.noapp_role_id")
        );

        // ---- STAFF APPROVE MESSAGES ----
        ApproveMessages approveMessages = new ApproveMessages(
                join(c, "Messages.Staff.approve.success"),
                join(c, "Messages.Staff.approve.failed"),
                join(c, "Messages.Staff.approve.error"),
                join(c, "Messages.Staff.rewhitelist.timeout")
        );

        StaffMessages staffMessages = new StaffMessages(
                approveMessages,
                join(c, "Messages.Staff.applicant_error")
        );

        // ---- PLAYER MESSAGES ----
        List<Map<?, ?>> rawReasons =
                c.getMapList("Messages.Player.denied_reasons");

        List<DeniedReason> deniedReasons = new ArrayList<>();

        for (Map<?, ?> entry : rawReasons) {
            @SuppressWarnings("unchecked")
            List<String> lines = (List<String>) entry.get("message");

            deniedReasons.add(new DeniedReason(
                    (String) entry.get("label"),
                    String.join("\n", lines)
            ));
        }

        PlayerMessages playerMessages = new PlayerMessages(
                join(c, "Messages.Player.approved"),
                deniedReasons,
                join(c, "Messages.Player.more_info")
        );
        // ---- LOG MESSAGES ----
        LogMessages logMessages = new LogMessages(
                join(c, "Messages.Logs.whitelist")
        );
        MessagesConfig messages = new MessagesConfig(
                staffMessages,
                playerMessages,
                logMessages
        );

        return new WhitelistConfig(
                bot,
                channels,
                roles,
                messages
        );
    }
}

