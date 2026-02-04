package tech.purelove.twswhitelist.discord.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import tech.purelove.twswhitelist.config.WhitelistConfig;

public final class Permissions {

    private Permissions() {}

    public static boolean requireStaff(
            IReplyCallback event,
            WhitelistConfig config
    ) {

        Member member = event.getMember();
        if (member == null) {
            // Webhook message interaction — actor is still a user
            User user = event.getUser();
            Guild guild = event.getGuild();
            if (guild == null) return false;

            member = guild.getMember(user);
        }

        if (member == null) return false;

        boolean isStaff = member.getRoles().stream()
                .anyMatch(r -> r.getId().equals(config.roles().staffRoleId()));

        if (!isStaff) {
            event.reply("❌ You do not have permission to use this.")
                    .setEphemeral(true)
                    .queue();
            return false;
        }

        return true;
    }
}
