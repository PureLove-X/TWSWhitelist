package tech.purelove.twswhitelist.config.messages.player;

import java.util.List;

public record PlayerMessages(
        String approved,
        List<DeniedReason> deniedReasons,
        String moreInfo
) {}
