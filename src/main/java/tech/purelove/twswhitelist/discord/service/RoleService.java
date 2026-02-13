package tech.purelove.twswhitelist.discord.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import tech.purelove.twswhitelist.config.WhitelistConfig;

public final class RoleService {

    private RoleService() {}

    public static void applyApproved(Member member, WhitelistConfig config) {
        Guild guild = member.getGuild();

        Role approved = guild.getRoleById(config.roles().approvedRoleId());
        Role noApp = guild.getRoleById(config.roles().noappRoleId());
        Role denied = guild.getRoleById(config.roles().deniedRoleId());

        if (approved != null) {
            guild.addRoleToMember(member, approved).queue();
        }

        if (noApp != null) {
            guild.removeRoleFromMember(member, noApp).queue();
        }

        if (denied != null) {
            guild.removeRoleFromMember(member, denied).queue();
        }
    }
    public static void applyDenied(Member member, WhitelistConfig config) {
        Guild guild = member.getGuild();

        Role approved = guild.getRoleById(config.roles().approvedRoleId());
        Role denied = guild.getRoleById(config.roles().deniedRoleId());
        Role noApp  = guild.getRoleById(config.roles().noappRoleId());

        if (denied != null) {
            guild.addRoleToMember(member, denied).queue();
        }
        if (approved != null) {
            guild.removeRoleFromMember(member, approved).queue();
        }
        if (noApp != null) {
            guild.removeRoleFromMember(member, noApp).queue();
        }
    }

}
