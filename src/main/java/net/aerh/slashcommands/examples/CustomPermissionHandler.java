package net.aerh.slashcommands.examples;

import net.aerh.slashcommands.api.permissions.PermissionChecker;
import net.aerh.slashcommands.api.permissions.PermissionHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.OffsetDateTime;

/**
 * Example custom permission handler showing how to implement custom permission logic
 */
public class CustomPermissionHandler implements PermissionHandler {

    private final String botOwnerId;

    public CustomPermissionHandler(String botOwnerId) {
        this.botOwnerId = botOwnerId;
    }

    @Override
    public PermissionChecker.PermissionResult checkPermission(SlashCommandInteractionEvent event, String permission) {
        String userId = event.getUser().getId();
        Member member = event.getMember();

        switch (permission) {
            case "BOT_OWNER":
                if (botOwnerId.equals(userId)) {
                    return PermissionChecker.PermissionResult.allowed();
                } else {
                    return PermissionChecker.PermissionResult.denied(
                            "Only the bot owner can use this command", permission);
                }

            case "PREMIUM_USER":
                // Example: Check if user has a premium role
                if (member != null && member.getRoles().stream()
                        .anyMatch(role -> role.getName().equalsIgnoreCase("Premium") ||
                                role.getName().equalsIgnoreCase("VIP") ||
                                role.getName().equalsIgnoreCase("Supporter"))) {
                    return PermissionChecker.PermissionResult.allowed();
                } else {
                    return PermissionChecker.PermissionResult.denied(
                            "This command requires a Premium subscription", permission);
                }

            case "LEVEL_10":
                // Example: Check user level from a database or cache
                // For this example, we'll just check if they have a specific role
                if (member != null && member.getRoles().stream()
                        .anyMatch(role -> role.getName().contains("Level") &&
                                extractLevelFromRoleName(role.getName()) >= 10)) {
                    return PermissionChecker.PermissionResult.allowed();
                } else {
                    return PermissionChecker.PermissionResult.denied(
                            "You need to be at least level 10 to use this command", permission);
                }

            case "TRUSTED_USER":
                // Example: Check if user has been in the server for more than 30 days
                if (member != null && member.getTimeJoined().isBefore(
                        OffsetDateTime.now().minusDays(30))) {
                    return PermissionChecker.PermissionResult.allowed();
                } else {
                    return PermissionChecker.PermissionResult.denied(
                            "You need to be a member for at least 30 days to use this command", permission);
                }

            default:
                // This handler doesn't recognise the permission
                return PermissionChecker.PermissionResult.denied("Unknown permission: " + permission, permission);
        }
    }

    @Override
    public boolean canHandle(String permission) {
        // Define which permissions this handler can check
        return permission.equals("BOT_OWNER") ||
                permission.equals("PREMIUM_USER") ||
                permission.equals("LEVEL_10") ||
                permission.equals("TRUSTED_USER");
    }

    @Override
    public int getPriority() {
        // Higher priority than default handlers
        return 100;
    }

    private int extractLevelFromRoleName(String roleName) {
        try {
            // Extract number from role names like "Level 15" or "Lvl 20"
            String[] parts = roleName.split(" ");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    return Integer.parseInt(part);
                }
            }
        } catch (Exception ignored) {
        }

        return 0;
    }
}