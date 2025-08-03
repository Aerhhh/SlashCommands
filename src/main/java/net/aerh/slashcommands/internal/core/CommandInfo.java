package net.aerh.slashcommands.internal.core;

import net.aerh.slashcommands.api.annotations.SlashCommand;
import net.aerh.slashcommands.api.annotations.SlashOption;
import net.aerh.slashcommands.internal.validation.OptionTypeMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a registered slash command, including its method, annotation data, and options.
 * This class is used internally by the framework to store and manage command metadata.
 */
public class CommandInfo {
    private static final Logger logger = LoggerFactory.getLogger(CommandInfo.class);
    
    private final Object instance;
    private final Method method;
    private final SlashCommand annotation;
    private final List<OptionInfo> options;

    public CommandInfo(Object instance, Method method, SlashCommand annotation) {
        this.instance = instance;
        this.method = method;
        this.annotation = annotation;
        this.options = new ArrayList<>();

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(SlashOption.class)) {
                SlashOption optionAnnotation = param.getAnnotation(SlashOption.class);

                boolean nameInferred = optionAnnotation.name().isEmpty();
                String optionName = nameInferred ? param.getName() : optionAnnotation.name();
                String convertedName = camelCaseToSnakeCase(optionName);

                if (!nameInferred && !optionName.equals(convertedName)) {
                    logger.warn("Option name '{}' in parameter '{}' in method '{}' does not conform to Discord Slash Option standards. " +
                               "Discord requires option names to be lowercase with underscores/hyphens only. " +
                               "This has been automatically converted to '{}' but may break in future versions.",
                               optionName, param.getName(), method.getName(), convertedName);
                }

                OptionType resolvedType = optionAnnotation.type() == OptionType.UNKNOWN ? OptionTypeMapping.inferOptionType(param.getType()) : optionAnnotation.type();
                options.add(new OptionInfo(param, optionAnnotation, i, convertedName, resolvedType));
            }
        }
    }

    private static String camelCaseToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));

        for (int i = 1; i < camelCase.length(); i++) {
            char current = camelCase.charAt(i);
            if (Character.isUpperCase(current)) {
                result.append('_').append(Character.toLowerCase(current));
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }

    /**
     * Gets the instance containing the command method.
     *
     * @return the command handler instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * Gets the method that handles this command.
     *
     * @return the command handler method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the {@link SlashCommand} annotation for this command.
     *
     * @return the command annotation
     */
    public SlashCommand getAnnotation() {
        return annotation;
    }

    /**
     * Gets the list of options for this command.
     *
     * @return list of option information
     */
    public List<OptionInfo> getOptions() {
        return options;
    }

    /**
     * Gets the full command path including group and subcommand if applicable.
     *
     * @return the full command path (e.g., "example group subcommand")
     */
    public String getFullCommandPath() {
        StringBuilder path = new StringBuilder(annotation.name());
        if (!annotation.group().isEmpty()) {
            path.append(" ").append(annotation.group());
        }
        if (!annotation.subcommand().isEmpty()) {
            path.append(" ").append(annotation.subcommand());
        }
        return path.toString();
    }

    public record OptionInfo(Parameter parameter, SlashOption annotation, int parameterIndex, String resolvedName,
                             OptionType resolvedType) {
    }
}