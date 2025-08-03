package net.aerh.slashcommands.internal.validation;

import net.aerh.slashcommands.api.annotations.SlashCommand;
import net.aerh.slashcommands.api.annotations.SlashOption;
import net.aerh.slashcommands.internal.utils.StringUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for slash command methods and their annotations.
 */
public class CommandValidator {
    public static final int MAX_STRING_LENGTH = 6_000;
    private static final Logger logger = LoggerFactory.getLogger(CommandValidator.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_-]{1,32}$");
    private static final int MAX_DESCRIPTION_LENGTH = 100;
    private static final int MAX_OPTIONS = 25;
    private static final int MAX_CHOICES = 25;

    /**
     * Validates a slash command method and its annotations.
     *
     * @param method the method to validate
     * @return list of validation errors, empty if valid
     */
    public static List<ValidationError> validateCommand(Method method) {
        List<ValidationError> errors = new ArrayList<>();

        if (!method.isAnnotationPresent(SlashCommand.class)) {
            errors.add(new ValidationError(method, "Method must be annotated with @SlashCommand"));
            return errors;
        }

        SlashCommand command = method.getAnnotation(SlashCommand.class);
        validateCommandAnnotation(method, command, errors);
        validateParameters(method, errors);

        logValidationResult(command, errors);
        return errors;
    }

    private static void validateCommandAnnotation(Method method, SlashCommand command, List<ValidationError> errors) {
        validateName(method, command.name(), "Command", errors);
        validateDescription(method, command.description(), "Command", errors);
        validateSubcommandStructure(method, command, errors);
        validateMethodSignature(method, errors);
    }

    private static void validateParameters(Method method, List<ValidationError> errors) {
        Parameter[] parameters = method.getParameters();

        // Validate that first parameter is SlashCommandInteractionEvent
        if (parameters.length == 0) {
            errors.add(new ValidationError(method, "Command methods must have SlashCommandInteractionEvent as the first parameter"));
            return;
        }

        Parameter firstParam = parameters[0];
        if (!firstParam.getType().equals(SlashCommandInteractionEvent.class)) {
            errors.add(new ValidationError(method, "First parameter must be SlashCommandInteractionEvent, but was: " + firstParam.getType().getSimpleName()));
        }

        List<SlashOption> options = new ArrayList<>();

        // Skip the first parameter (SlashCommandInteractionEvent) when validating options
        for (int i = 1; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(SlashOption.class)) {
                SlashOption option = param.getAnnotation(SlashOption.class);
                options.add(option);
                validateOption(method, param, option, errors);
            }
        }

        if (options.size() > MAX_OPTIONS) {
            errors.add(new ValidationError(method, "Commands cannot have more than " + MAX_OPTIONS + " options"));
        }

        validateOptionOrder(method, options, errors);
    }

    private static void validateOption(Method method, Parameter param, SlashOption option, List<ValidationError> errors) {
        String paramName = param.getName();
        String rawName = option.name().isEmpty() ? paramName : option.name();
        String resolvedName = StringUtils.camelCaseToSnakeCase(rawName);

        validateName(method, resolvedName, "Option", paramName, errors);
        validateDescription(method, option.description(), "Option", paramName, errors);
        validateTypeCompatibility(method, param, option, errors);
        validateChoicesAndAutocomplete(method, option, paramName, errors);
        validateStringLimits(method, option, paramName, errors);
        validateNumericLimits(method, param, option, paramName, errors);
    }

    private static void validateName(Method method, String name, String type, List<ValidationError> errors) {
        validateName(method, name, type, null, errors);
    }

    private static void validateName(Method method, String name, String type, String paramName, List<ValidationError> errors) {
        String context = paramName != null ? " for parameter: " + paramName : "";

        if (name.isEmpty()) {
            errors.add(new ValidationError(method, type + " name cannot be empty" + context));
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            errors.add(new ValidationError(method, type + " name must be 1-32 characters, lowercase, and contain only letters, numbers, underscores, and hyphens" + context));
        }
    }

    private static void validateDescription(Method method, String description, String type, List<ValidationError> errors) {
        validateDescription(method, description, type, null, errors);
    }

    private static void validateDescription(Method method, String description, String type, String paramName, List<ValidationError> errors) {
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            String context = paramName != null ? " for parameter: " + paramName : "";
            errors.add(new ValidationError(method, type + " description must be " + MAX_DESCRIPTION_LENGTH + " characters or less" + context));
        }
    }

    private static void validateTypeCompatibility(Method method, Parameter param, SlashOption option, List<ValidationError> errors) {
        OptionType resolvedType = option.type() == OptionType.UNKNOWN ?
                OptionTypeMapping.inferOptionType(param.getType()) : option.type();

        if (!OptionTypeMapping.isCompatibleType(param.getType(), resolvedType)) {
            String compatibleTypes = OptionTypeMapping.getCompatibleTypesDescription(resolvedType);
            errors.add(new ValidationError(method, "Parameter type " + param.getType().getSimpleName() +
                    " is not compatible with option type " + resolvedType + " for parameter: " + param.getName() +
                    ". Compatible types: " + compatibleTypes));
        }
    }

    private static void validateChoicesAndAutocomplete(Method method, SlashOption option, String paramName, List<ValidationError> errors) {
        if (option.choices().length > 0 && !option.autocompleteId().isEmpty()) {
            errors.add(new ValidationError(method, "Options cannot have both choices and autocomplete (autocompleteId) for parameter: " + paramName));
        }

        if (option.choices().length > MAX_CHOICES) {
            errors.add(new ValidationError(method, "Options cannot have more than " + MAX_CHOICES + " choices for parameter: " + paramName));
        }

        validateChoiceContent(method, option, paramName, errors);
    }

    private static void validateStringLimits(Method method, SlashOption option, String paramName, List<ValidationError> errors) {
        Parameter param = findParameterByName(method, paramName);
        if (param == null) {
            return;
        }

        OptionType resolvedType = option.type() == OptionType.UNKNOWN ?
                OptionTypeMapping.inferOptionType(param.getType()) : option.type();

        if (resolvedType != OptionType.STRING) {
            return;
        }

        int minLength = option.minLength();
        int maxLength = option.maxLength();

        if (minLength < 0 || minLength > MAX_STRING_LENGTH) {
            errors.add(new ValidationError(method, "String option minLength must be between 0 and " + MAX_STRING_LENGTH + " for parameter: " + paramName));
        }

        if (maxLength < 1 || maxLength > MAX_STRING_LENGTH) {
            errors.add(new ValidationError(method, "String option maxLength must be between 1 and " + MAX_STRING_LENGTH + " for parameter: " + paramName));
        }

        if (minLength > maxLength) {
            errors.add(new ValidationError(method, "String option minLength cannot be greater than maxLength for parameter: " + paramName));
        }
    }

    private static void validateOptionOrder(Method method, List<SlashOption> options, List<ValidationError> errors) {
        boolean foundOptional = false;

        for (SlashOption option : options) {
            if (!option.required()) {
                foundOptional = true;
            } else if (foundOptional) {
                errors.add(new ValidationError(method, "Required options must come before optional options"));
                break;
            }
        }
    }

    private static Parameter findParameterByName(Method method, String paramName) {
        for (Parameter param : method.getParameters()) {
            if (param.getName().equals(paramName)) {
                return param;
            }
        }

        return null;
    }

    private static void logValidationResult(SlashCommand command, List<ValidationError> errors) {
        if (!errors.isEmpty()) {
            logger.error("Command validation failed for: /{} with {} errors: {}", command.name(), errors.size(), errors.stream().map(ValidationError::message).toList());
        }
    }

    private static void validateSubcommandStructure(Method method, SlashCommand command, List<ValidationError> errors) {
        String group = command.group();
        String subcommand = command.subcommand();

        // Validate subcommand name if present
        if (!subcommand.isEmpty()) {
            validateName(method, subcommand, "Subcommand", errors);
        }

        // Validate group name if present
        if (!group.isEmpty()) {
            validateName(method, group, "Subcommand group", errors);
            
            // Group requires subcommand
            if (subcommand.isEmpty()) {
                errors.add(new ValidationError(method, "Subcommand group '" + group + "' requires a subcommand name"));
            }
        }
    }

    private static void validateMethodSignature(Method method, List<ValidationError> errors) {
        // Validate return type
        if (!method.getReturnType().equals(void.class)) {
            errors.add(new ValidationError(method, "Slash command methods must return void, but found: " + method.getReturnType().getSimpleName()));
        }

        // Validate method visibility
        if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
            errors.add(new ValidationError(method, "Slash command methods must be public"));
        }

        // Validate not static
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            errors.add(new ValidationError(method, "Slash command methods cannot be static"));
        }
    }

    private static void validateNumericLimits(Method method, Parameter param, SlashOption option, String paramName, List<ValidationError> errors) {
        OptionType resolvedType = option.type() == OptionType.UNKNOWN ?
                OptionTypeMapping.inferOptionType(param.getType()) : option.type();

        if (resolvedType != OptionType.INTEGER && resolvedType != OptionType.NUMBER) {
            return;
        }

        int minValue = option.minValue();
        int maxValue = option.maxValue();

        // Check if limits are set (not default values)
        boolean hasMinValue = minValue != Integer.MIN_VALUE;
        boolean hasMaxValue = maxValue != Integer.MAX_VALUE;

        if (hasMinValue && hasMaxValue && minValue > maxValue) {
            errors.add(new ValidationError(method, "Numeric option minValue cannot be greater than maxValue for parameter: " + paramName));
        }
    }

    private static void validateChoiceContent(Method method, SlashOption option, String paramName, List<ValidationError> errors) {
        String[] choices = option.choices();
        
        for (int i = 0; i < choices.length; i++) {
            String choice = choices[i];
            
            // Check choice length
            if (choice.length() > 100) {
                errors.add(new ValidationError(method, "Choice '" + choice + "' is too long (max 100 characters) for parameter: " + paramName));
            }
            
            if (choice.isEmpty()) {
                errors.add(new ValidationError(method, "Choice at index " + i + " cannot be empty for parameter: " + paramName));
            }
            
            // Check for duplicate choices
            for (int j = i + 1; j < choices.length; j++) {
                if (choice.equals(choices[j])) {
                    errors.add(new ValidationError(method, "Duplicate choice '" + choice + "' found for parameter: " + paramName));
                    break;
                }
            }
        }
    }



    /**
     * Represents a validation error found in a slash command method.
     *
     * @param method  the method that failed validation
     * @param message the validation error message
     */
    public record ValidationError(Method method, String message) {
        @NotNull
        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(): " + message;
        }
    }
}
