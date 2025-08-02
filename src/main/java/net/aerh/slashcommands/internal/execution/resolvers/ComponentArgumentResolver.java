package net.aerh.slashcommands.internal.execution.resolvers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Resolves arguments for component interaction handler method invocation.
 * Handles mapping component interaction data to Java method parameters.
 */
public class ComponentArgumentResolver implements ArgumentResolver<Object> {

    @Override
    public Object[] prepareArguments(Method method, Object event) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.getType().isAssignableFrom(event.getClass())) {
                args[i] = event;
            }
        }

        return args;
    }
}