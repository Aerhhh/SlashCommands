package net.aerh.slashcommands.internal.core;

import net.aerh.slashcommands.api.permissions.PermissionChecker;
import net.aerh.slashcommands.api.permissions.PermissionHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages custom permission handlers and provides extensible permission checking.
 *
 * <p>The PermissionManager allows registration of custom {@link PermissionHandler} implementations
 * to extend the framework's permission system beyond Discord's built-in permissions and roles.
 * Handlers are prioritised, with higher priority handlers being checked first.
 */
public class PermissionManager {
    private static final Logger logger = LoggerFactory.getLogger(PermissionManager.class);
    private final List<PermissionHandler> handlers = new ArrayList<>();

    /**
     * Registers a custom permission handler.
     * Handlers are automatically sorted by priority (highest first).
     *
     * @param handler the {@link PermissionHandler} to register
     */
    public void registerHandler(PermissionHandler handler) {
        handlers.add(handler);
        handlers.sort(Comparator.comparingInt(PermissionHandler::getPriority).reversed());
        logger.info("Registered permission handler: {} (priority: {})", handler.getClass().getSimpleName(), handler.getPriority());
    }

    /**
     * Unregisters a custom permission handler.
     *
     * @param handler the permission handler to unregister
     */
    public void unregisterHandler(PermissionHandler handler) {
        handlers.remove(handler);
        logger.info("Unregistered permission handler: {}", handler.getClass().getSimpleName());
    }

    /**
     * Checks permissions for a command interaction.
     * First checks built-in Discord permissions and roles, then delegates to custom handlers if needed.
     *
     * @param event               the slash command interaction event
     * @param requiredPermissions array of required permission strings
     * @return the permission check result
     */
    public PermissionChecker.PermissionResult checkPermissions(SlashCommandInteractionEvent event, String[] requiredPermissions) {
        PermissionChecker.PermissionResult result = PermissionChecker.checkPermissions(event, requiredPermissions);

        if (!result.wasSuccessful()) {
            for (String permission : requiredPermissions) {
                for (PermissionHandler handler : handlers) {
                    if (handler.canHandle(permission)) {
                        PermissionChecker.PermissionResult customResult = handler.checkPermission(event, permission);

                        if (!customResult.wasSuccessful()) {
                            return customResult;
                        }
                    }
                }

                if (permission.equals(result.getFailedPermission())) {
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * Gets all registered permission handlers.
     *
     * @return a copy of the list of registered permission handlers, ordered by priority (highest first)
     */
    public List<PermissionHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }

    /**
     * Clears all registered permission handlers.
     * This method is typically called during application shutdown.
     */
    public void clearHandlers() {
        handlers.clear();
    }
}