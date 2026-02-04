package tech.purelove.twswhitelist.config;

import tech.purelove.twswhitelist.config.bot.BotConfig;
import tech.purelove.twswhitelist.config.channels.ChannelConfig;
import tech.purelove.twswhitelist.config.messages.MessagesConfig;
import tech.purelove.twswhitelist.config.roles.RoleConfig;

public record WhitelistConfig(
        BotConfig bot,
        ChannelConfig channels,
        RoleConfig roles,
        MessagesConfig messages
) {}