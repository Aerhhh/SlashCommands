package net.aerh.slashcommands.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a modal interaction handler.
 *
 * <p>Modal handlers are called when users submit modal dialogues (forms)
 * that have matching modal IDs. The framework supports both exact ID matching
 * and wildcard pattern matching for dynamic modal handling.
 *
 * <p>Methods annotated with this annotation should have the following signature:
 * <ul>
 *   <li>First parameter: {@link net.dv8tion.jda.api.events.interaction.ModalInteractionEvent}</li>
 *   <li>Subsequent parameters: String values corresponding to modal input fields in order</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * @SlashModalHandler(id = "feedback_form")
 * public void handleFeedback(ModalInteractionEvent event, String subject, String message) {
 *     event.reply("Thank you for your feedback: " + subject).setEphemeral(true).queue();
 * }
 *
 * // Pattern matching for dynamic modals
 * @SlashModalHandler(id = "user_report", patterns = {"user_report_*"})
 * public void handleUserReport(ModalInteractionEvent event, String category, String description) {
 *     String userId = event.getModalId().replace("user_report_", "");
 *     // Handle user-specific reports
 * }
 * }</pre>
 *
 * @see net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
 * @see net.dv8tion.jda.api.interactions.modals.Modal
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashModalHandler {
    /**
     * The modal ID to handle.
     * This should match the ID used when creating the modal with {@link net.dv8tion.jda.api.interactions.modals.Modal#create(String, String)}.
     *
     * @return the modal ID to match against
     */
    String id();

    /**
     * Optional patterns for more complex modal ID matching.
     * Supports wildcard matching with '*' character for dynamic modal handling.
     * When patterns are specified, the handler will be called for any modal ID
     * that matches any of the provided patterns.
     *
     * @return array of patterns to match modal IDs against, empty by default
     */
    String[] patterns() default {};
}