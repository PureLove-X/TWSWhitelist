package tech.purelove.twswhitelist.discord.service;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import tech.purelove.twswhitelist.discord.ids.DashboardIds;

import java.awt.*;

public final class DashboardBuilder {

    private DashboardBuilder() {}

    public static void send(MessageChannel channel) {
        channel.sendMessage("""
                        🛠 **Staff Dashboard**
                        Choose an action below:
                        """)
                .addActionRow(
                        Button.success(DashboardIds.APPROVE, "Approve"),
                        Button.danger(DashboardIds.DENY, "Deny"),
                        Button.secondary(DashboardIds.MORE_INFO, "More Info")
                )
                .queue(msg -> msg.pin().queue());

    }
}