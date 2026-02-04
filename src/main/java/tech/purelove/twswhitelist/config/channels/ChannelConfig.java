package tech.purelove.twswhitelist.config.channels;

public record ChannelConfig(
        String applicationCategoryId,
        String whitelistLogChannelId,
        String rewhitelistLogChannelId
) {}
