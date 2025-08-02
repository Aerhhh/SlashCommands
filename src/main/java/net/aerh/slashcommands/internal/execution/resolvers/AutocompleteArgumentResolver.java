package net.aerh.slashcommands.internal.execution.resolvers;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Resolves arguments for autocomplete handler method invocation.
 * Handles mapping autocomplete interaction data to Java method parameters.
 */
public class AutocompleteArgumentResolver implements ArgumentResolver<CommandAutoCompleteInteractionEvent> {

    @Override
    public Object[] prepareArguments(Method method, CommandAutoCompleteInteractionEvent event) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.getType() == CommandAutoCompleteInteractionEvent.class) {
                args[i] = event;
            } else if (param.getType() == String.class) {
                args[i] = event.getFocusedOption().getValue();
            }
        }

        return args;
    }
}