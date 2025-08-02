package net.aerh.slashcommands.api.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

/**
 * Handles built-in permission checking for Discord permissions and role-based permissions.
 * This class provides the core permission checking logic used by the framework before
 * delegating to custom permission handlers.
 */
public class PermissionChecker {
    /**
     * Checks Discord permissions and role-based permissions for a command interaction.
     *
     * @param event               the slash command interaction event
     * @param requiredPermissions array of required permission strings
     * @return the permission check result
     */
    public static PermissionResult checkPermissions(SlashCommandInteractionEvent event, String[] requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return PermissionResult.allowed();
        }

        Member member = event.getMember();

        if (member == null) {
            return checkDMPermissions(requiredPermissions);
        }

        return checkGuildPermissions(member, requiredPermissions);
    }

    private static PermissionResult checkDMPermissions(String[] requiredPermissions) {
        for (String permission : requiredPermissions) {
            if (isDiscordPermission(permission)) {
                return PermissionResult.denied("Discord permissions don't apply in DMs", permission);
            }

            if (permission.startsWith("ROLE:")) {
                return PermissionResult.denied("Role permissions don't apply in DMs", permission);
            }

            // All other permissions (custom ones) should be handled by custom PermissionHandlers
            return PermissionResult.denied("Unknown permission type: " + permission, permission);
        }

        return PermissionResult.allowed();
    }

    private static PermissionResult checkGuildPermissions(Member member, String[] requiredPermissions) {
        for (String permission : requiredPermissions) {
            PermissionResult result = checkSinglePermission(member, permission);
            if (!result.wasSuccessful()) {
                return result;
            }
        }

        return PermissionResult.allowed();
    }

    private static PermissionResult checkSinglePermission(Member member, String permission) {
        if (isDiscordPermission(permission)) {
            return checkDiscordPermission(member, permission);
        }

        if (permission.startsWith("ROLE:")) {
            return checkRolePermission(member, permission);
        }

        // All other permissions should be handled by custom PermissionHandlers
        return PermissionResult.denied("Unknown permission type: " + permission, permission);
    }

    private static PermissionResult checkDiscordPermission(Member member, String permission) {
        try {
            Permission discordPerm = Permission.valueOf(permission);
            if (member.hasPermission(discordPerm)) {
                return PermissionResult.allowed();
            } else {
                return PermissionResult.denied("Missing Discord permission: " + permission, permission);
            }
        } catch (IllegalArgumentException e) {
            return PermissionResult.denied("Invalid Discord permission: " + permission, permission);
        }
    }

    private static PermissionResult checkRolePermission(Member member, String permission) {
        String roleName = permission.substring(5);
        List<Role> roles = member.getRoles();

        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return PermissionResult.allowed();
            }
        }

        return PermissionResult.denied("Missing required role: " + roleName, permission);
    }

    private static boolean isDiscordPermission(String permission) {
        try {
            Permission.valueOf(permission);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Result of a permission check operation.
     * Contains information about whether the permission check passed and why it failed if applicable.
     */
    public static class PermissionResult {
        private final boolean allowed;
        private final String reason;
        private final String failedPermission;

        private PermissionResult(boolean allowed, String reason, String failedPermission) {
            this.allowed = allowed;
            this.reason = reason;
            this.failedPermission = failedPermission;
        }

        /**
         * Creates a successful permission result.
         *
         * @return a permission result indicating success
         */
        public static PermissionResult allowed() {
            return new PermissionResult(true, null, null);
        }

        /**
         * Creates a failed permission result.
         *
         * @param reason           the reason for the failure
         * @param failedPermission the permission that failed
         * @return a permission result indicating failure
         */
        public static PermissionResult denied(String reason, String failedPermission) {
            return new PermissionResult(false, reason, failedPermission);
        }

        /**
         * Checks if the permission check was successful.
         *
         * @return true if permission was granted, false otherwise
         */
        public boolean wasSuccessful() {
            return allowed;
        }

        /**
         * Gets the reason for permission failure.
         *
         * @return the failure reason, or null if permission was granted
         */
        public String getReason() {
            return reason;
        }

        /**
         * Gets the specific permission that failed.
         *
         * @return the failed permission string, or null if permission was granted
         */
        public String getFailedPermission() {
            return failedPermission;
        }

        @Override
        public String toString() {
            if (allowed) {
                return "PermissionResult{allowed=true}";
            } else {
                return "PermissionResult{allowed=false, reason='" + reason + "', failedPermission='" + failedPermission + "'}";
            }
        }
    }
}