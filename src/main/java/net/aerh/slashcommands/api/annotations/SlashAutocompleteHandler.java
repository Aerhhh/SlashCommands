package net.aerh.slashcommands.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an autocomplete handler for a specific command option.
 *
 * <p>Autocomplete methods are called when users are typing in a command option
 * that has autocomplete enabled. The method should return a List of {@link net.dv8tion.jda.api.interactions.commands.Command.Choice}
 * objects representing the available suggestions.
 *
 * <p>The autocomplete handler is linked to command options via a unique ID that must
 * be specified in both the {@link SlashAutocompleteHandler} annotation and the corresponding
 * {@link SlashOption} annotation's <code>autocompleteId</code> field.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Define the autocomplete handler
 * @SlashAutocompleteHandler(id = "search_suggestions")
 * public List<Command.Choice> searchAutocomplete(CommandAutoCompleteInteractionEvent event) {
 *     String input = event.getFocusedOption().getValue();
 *     return Stream.of("apple", "banana", "cherry", "date")
 *         .filter(item -> item.startsWith(input.toLowerCase()))
 *         .map(item -> new Command.Choice(item, item))
 *         .collect(Collectors.toList());
 * }
 *
 * // Link it to a command option
 * @SlashCommand(name = "search", description = "Search for items")
 * public void search(SlashCommandInteractionEvent event, @SlashOption(description = "Search query", autocompleteId = "search_suggestions") String query) {
 *     // Implementation
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashAutocompleteHandler {

    /**
     * The unique identifier for this autocomplete handler.
     * This ID must match the <pre>autocompleteId</pre> specified in the corresponding {@link SlashOption} annotation.
     *
     * @return the autocomplete handler ID
     */
    String id();
}