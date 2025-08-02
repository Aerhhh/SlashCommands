package net.aerh.slashcommands.internal.execution.resolvers;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Resolves arguments for modal interaction handler method invocation.
 * Handles mapping modal interaction data to Java method parameters.
 */
public class ModalArgumentResolver implements ArgumentResolver<ModalInteractionEvent> {

    @Override
    public Object[] prepareArguments(Method method, ModalInteractionEvent event) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.getType() == ModalInteractionEvent.class) {
                args[i] = event;
            } else if (param.getType() == String.class) {
                // For string parameters, try to match with modal input values
                // This is a simple implementation - could be enhanced with parameter name matching
                var values = event.getValues();
                if (i - 1 < values.size()) { // -1 because first param is usually the event
                    args[i] = values.get(i - 1).getAsString();
                }
            }
        }

        return args;
    }
}