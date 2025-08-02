package net.aerh.slashcommands.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a component interaction handler for buttons and select menus.
 *
 * <p>Component handlers are called when users interact with buttons or select menus
 * that have matching component IDs. The framework supports both exact ID matching
 * and wildcard pattern matching for dynamic component handling.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SlashComponentHandler(id = "approve_button")
 * public void handleApproval(ButtonInteractionEvent event) {
 *     event.reply("Approved!").setEphemeral(true).queue();
 * }
 *
 * @SlashComponentHandler(id = "colour_select")
 * public void handleColourSelection(StringSelectInteractionEvent event) {
 *     String colour = event.getValues().get(0);
 *     event.reply("You selected: " + colour).setEphemeral(true).queue();
 * }
 *
 * // Pattern matching for dynamic components
 * @SlashComponentHandler(patterns = {"warn_*", "ban_*"})
 * public void handleModerationActions(ButtonInteractionEvent event) {
 *     String componentId = event.getComponentId();
 *     // Handle multiple related components
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashComponentHandler {

    /**
     * The component ID to handle.
     * Must match the ID used when creating the button or select menu component.
     * Used for exact matching unless patterns are specified.
     *
     * @return the component ID
     */
    String id();

    /**
     * Optional patterns for handling multiple component IDs with wildcards.
     * Supports asterisk (*) wildcards for pattern matching.
     * When patterns are specified, both the exact ID and patterns will be matched.
     *
     * <p>Examples:
     * <ul>
     *   <li>{"warn_*"} matches "warn_123", "warn_user", etc.</li>
     *   <li>{"*_confirm", "*_cancel"} matches "delete_confirm", "ban_cancel", etc.</li>
     * </ul>
     *
     * @return array of wildcard patterns, or empty array if not used
     */
    String[] patterns() default {};
}