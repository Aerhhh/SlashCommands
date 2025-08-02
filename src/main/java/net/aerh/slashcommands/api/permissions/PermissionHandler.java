package net.aerh.slashcommands.api.permissions;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Interface for implementing custom permission handlers.
 *
 * <p>Permission handlers allow extending the framework's permission system beyond
 * Discord's built-in permissions and role-based permissions. Handlers are checked
 * in priority order, with higher priority handlers being checked first.
 *
 * <p>Example implementation:
 * <pre>{@code
 * public class CustomPermissionHandler implements PermissionHandler {
 *     @Override
 *     public boolean canHandle(String permission) {
 *         return permission.equals("BOT_OWNER") || permission.equals("PREMIUM_USER");
 *     }
 *
 *     @Override
 *     public PermissionChecker.PermissionResult checkPermission(SlashCommandInteractionEvent event, String permission) {
 *         String userId = event.getUser().getId();
 *
 *         if ("BOT_OWNER".equals(permission)) {
 *             return userId.equals("123456789") ?
 *                 PermissionResult.allow() :
 *                 PermissionResult.deny("Only the bot owner can use this command.");
 *         }
 *
 *         if ("PREMIUM_USER".equals(permission)) {
 *             boolean isPremium = checkPremiumStatus(userId);
 *             return isPremium ?
 *                 PermissionResult.allow() :
 *                 PermissionResult.deny("This command requires premium membership.");
 *         }
 *
 *         return PermissionResult.deny("Unknown permission: " + permission);
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return 100; // Higher priority
 *     }
 * }
 * }</pre>
 */
public interface PermissionHandler {

    /**
     * Checks if a user has the specified permission.
     * This method is only called if {@link #canHandle(String)} returns true for the permission.
     *
     * @param event      the slash command interaction event
     * @param permission the permission string to check
     * @return a {@link net.aerh.slashcommands.api.permissions.PermissionChecker.PermissionResult} indicating whether the permission check passed or failed
     */
    PermissionChecker.PermissionResult checkPermission(SlashCommandInteractionEvent event, String permission);

    /**
     * Gets the priority of this permission handler.
     * Handlers with higher priority values are checked before those with lower values.
     * The default priority is 0.
     *
     * @return the priority value (higher numbers = higher priority)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Determines whether this handler can process the given permission string.
     * This method is called before {@link #checkPermission(SlashCommandInteractionEvent, String)}
     * to determine if this handler should be used for a specific permission.
     *
     * @param permission the permission string to evaluate
     * @return true if this handler can process the permission, false otherwise
     */
    boolean canHandle(String permission);
}