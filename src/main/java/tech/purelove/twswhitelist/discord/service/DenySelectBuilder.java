package tech.purelove.twswhitelist.discord.service;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import tech.purelove.twswhitelist.config.messages.player.DeniedReason;
import tech.purelove.twswhitelist.config.WhitelistConfig;

public final class DenySelectBuilder {

    private DenySelectBuilder() {}

    public static StringSelectMenu build(WhitelistConfig config) {

        StringSelectMenu.Builder menu =
                StringSelectMenu.create("deny_reason")
                        .setPlaceholder("Select a denial reason");

        for (DeniedReason reason : config.messages()
                .player()
                .deniedReasons()) {

            menu.addOption(
                    reason.label(),   // what staff sees
                    reason.label()    // value we’ll match later
            );
        }

        return menu.build();
    }
}
