package tech.purelove.twswhitelist.config.messages;

import tech.purelove.twswhitelist.config.messages.logs.LogMessages;
import tech.purelove.twswhitelist.config.messages.player.PlayerMessages;
import tech.purelove.twswhitelist.config.messages.staff.StaffMessages;

public record MessagesConfig(
        StaffMessages staff,
        PlayerMessages player,
        LogMessages log
) {}