package net.aerh.slashcommands.internal.handlers;

import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.execution.resolvers.AutocompleteArgumentResolver;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Handler for Discord autocomplete interactions.
 * Manages autocomplete execution and error handling.
 */
public class AutocompleteHandler implements InteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AutocompleteHandler.class);

    private final SlashCommandRegistry registry;
    private final AutocompleteArgumentResolver argumentResolver;

    public AutocompleteHandler(SlashCommandRegistry registry) {
        this.registry = registry;
        this.argumentResolver = new AutocompleteArgumentResolver();
    }

    /**
     * Handles an autocomplete interaction event.
     *
     * @param event the autocomplete interaction event
     */
    public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
        String commandName = event.getName();
        String optionName = event.getFocusedOption().getName();

        // Find the autocomplete ID for this command and option
        String autocompleteId = registry.findAutocompleteId(commandName, optionName);
        if (autocompleteId == null) {
            return;
        }

        SlashCommandRegistry.AutocompleteInfo autocompleteInfo = registry.getAutocompleteHandler(autocompleteId);
        if (autocompleteInfo == null) {
            return;
        }

        try {
            executeAutocomplete(autocompleteInfo, event);
        } catch (Exception e) {
            logger.error("Error in autocomplete for '{}:{}': {}", commandName, optionName, e.getMessage(), e);
        }
    }

    private void executeAutocomplete(SlashCommandRegistry.AutocompleteInfo autocompleteInfo,
                                     CommandAutoCompleteInteractionEvent event) throws Exception {
        Method method = autocompleteInfo.method();
        Object[] args = argumentResolver.prepareArguments(method, event);
        Object result = method.invoke(autocompleteInfo.instance(), args);

        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<Command.Choice> choices = (List<Command.Choice>) result;
            event.replyChoices(choices).queue();
        } else {
            logger.error("Autocomplete method returned unexpected type: {}",
                    result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    @Override
    public String getHandlerType() {
        return "Autocomplete";
    }
}