package net.aerh.slashcommands.api.annotations;

import net.aerh.slashcommands.internal.validation.CommandValidator;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as a Discord slash command option.
 *
 * <p>Option names and types can be automatically inferred from the parameter name and type.
 * The framework automatically converts camelCase parameter names to snake_case option names
 * and infers the appropriate OptionType from the Java parameter type.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SlashCommand(name = "greet", description = "Greet a user")
 * public void greet(SlashCommandInteractionEvent event,
 *                   @SlashOption(description = "User to greet") User user,
 *                   @SlashOption(description = "Custom message", required = false) String message) {
 *     // Implementation
 * }
 *
 * @SlashCommand(name = "ban", description = "Ban a user")
 * public void ban(SlashCommandInteractionEvent event,
 *                 @SlashOption(description = "User to ban") User user,
 *                 @SlashOption(description = "Days of messages to delete",
 *                              minValue = 0, maxValue = 7) Integer deleteMessageDays) {
 *     // Implementation
 * }
 *
 * // Autocomplete example - autocomplete is automatically enabled when autocompleteId is specified
 * @SlashCommand(name = "give", description = "Give an item to a user")
 * public void giveItem(SlashCommandInteractionEvent event,
 *                      @SlashOption(description = "User to give item to") User user,
 *                      @SlashOption(description = "Item name", autocompleteId = "item_search") String item) {
 *     // Implementation
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashOption {

    /**
     * The name of the option as it appears to users.
     * If not specified, the parameter name will be converted from camelCase to snake_case.
     * Must be 1-32 characters, lowercase, and may contain dashes and underscores.
     *
     * @return the option name, or empty string to use the parameter name
     */
    String name() default "";

    /**
     * The description of the option shown to users.
     * Must be 1-100 characters.
     *
     * @return the option description
     */
    String description() default "No description provided";

    /**
     * The Discord option type.
     * If not specified (UNKNOWN), the type will be automatically inferred from the parameter type.
     *
     * @return the option type, or UNKNOWN
     */
    OptionType type() default OptionType.UNKNOWN;

    /**
     * Whether this option is required.
     * Required options must come before optional options in the method signature.
     *
     * @return true if required, false if optional
     */
    boolean required() default true;

    /**
     * Predefined choices for this option.
     * Cannot be used together with autocompleteId.
     * Limited to 25 choices maximum.
     *
     * @return array of choice strings
     */
    String[] choices() default {};

    /**
     * The ID of the autocomplete handler for this option.
     * When specified, autocomplete is automatically enabled for this option.
     * Must match the ID specified in a {@link SlashAutocompleteHandler} annotation.
     * Cannot be used together with predefined choices.
     *
     * @return the autocomplete handler ID, or empty string if autocomplete is not desired
     */
    String autocompleteId() default "";

    /**
     * Minimum value for INTEGER and NUMBER options.
     * Only applies to numeric option types.
     *
     * @return the minimum value, or Integer.MIN_VALUE if not set
     */
    int minValue() default Integer.MIN_VALUE;

    /**
     * Maximum value for INTEGER and NUMBER options.
     * Only applies to numeric option types.
     *
     * @return the maximum value, or Integer.MAX_VALUE if not set
     */
    int maxValue() default Integer.MAX_VALUE;

    /**
     * Minimum length for STRING options.
     * Must be between 0 and 6,000 characters.
     *
     * @return the minimum length, or 0 if not set
     */
    int minLength() default 0;

    /**
     * Maximum length for STRING options.
     * Must be between 1 and 6,000 characters.
     *
     * @return the maximum length, or 6,000 if not set
     */
    int maxLength() default CommandValidator.MAX_STRING_LENGTH;
}