package tech.purelove.twswhitelist.config.roles;

public record RoleConfig(
        String staffRoleId,
        String approvedRoleId,
        String deniedRoleId,
        String noappRoleId
) {}
