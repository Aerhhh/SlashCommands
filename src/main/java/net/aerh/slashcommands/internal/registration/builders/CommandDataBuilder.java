package net.aerh.slashcommands.internal.registration.builders;

import net.aerh.slashcommands.api.annotations.SlashOption;
import net.aerh.slashcommands.internal.core.CommandInfo;
import net.aerh.slashcommands.internal.validation.CommandValidator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class for creating Discord slash command data from CommandInfo objects.
 * Handles the conversion of framework annotations to JDA command structures.
 */
public class CommandDataBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CommandDataBuilder.class);

    /**
     * Creates a SlashCommandData object from command information.
     *
     * @param name                     command name
     * @param description              command description
     * @param nsfw                     whether the command is NSFW
     * @param defaultMemberPermissions default member permissions
     * @return configured SlashCommandData
     */
    public SlashCommandData createSlashCommandData(String name, String description, boolean nsfw, String[] defaultMemberPermissions) {
        SlashCommandData commandData = Commands.slash(name, description);

        if (nsfw) {
            commandData.setNSFW(true);
        }

        setDefaultPermissions(commandData, defaultMemberPermissions, name);
        return commandData;
    }

    /**
     * Creates a SubcommandData object from command information.
     *
     * @param commandInfo the command information
     * @return configured SubcommandData
     */
    public SubcommandData createSubcommandData(CommandInfo commandInfo) {
        SubcommandData subcommandData = new SubcommandData(
                commandInfo.getAnnotation().subcommand(),
                commandInfo.getAnnotation().description()
        );

        addOptionsToCommand(subcommandData, commandInfo);
        return subcommandData;
    }

    /**
     * Creates a SubcommandGroupData object.
     *
     * @param groupName   the group name
     * @param description the group description
     * @return configured SubcommandGroupData
     */
    public SubcommandGroupData createSubcommandGroupData(String groupName, String description) {
        return new SubcommandGroupData(groupName, description);
    }

    /**
     * Adds options from CommandInfo to a SlashCommandData.
     *
     * @param commandData the command data to modify
     * @param commandInfo the command information
     */
    public void addOptionsToCommand(SlashCommandData commandData, CommandInfo commandInfo) {
        for (CommandInfo.OptionInfo optionInfo : commandInfo.getOptions()) {
            OptionData optionData = createOptionData(
                    optionInfo.resolvedType(),
                    optionInfo.resolvedName(),
                    optionInfo.annotation().description(),
                    optionInfo.annotation().required(),
                    optionInfo.annotation()
            );
            commandData.addOptions(optionData);
        }
    }

    /**
     * Adds options from CommandInfo to a SubcommandData.
     *
     * @param subcommandData the subcommand data to modify
     * @param commandInfo    the command information
     */
    public void addOptionsToCommand(SubcommandData subcommandData, CommandInfo commandInfo) {
        for (CommandInfo.OptionInfo optionInfo : commandInfo.getOptions()) {
            OptionData optionData = createOptionData(
                    optionInfo.resolvedType(),
                    optionInfo.resolvedName(),
                    optionInfo.annotation().description(),
                    optionInfo.annotation().required(),
                    optionInfo.annotation()
            );
            subcommandData.addOptions(optionData);
        }
    }

    private OptionData createOptionData(OptionType type, String name, String description, boolean required, SlashOption annotation) {
        OptionData optionData = new OptionData(type, name, description, required);

        if (!annotation.autocompleteId().isEmpty()) {
            optionData.setAutoComplete(true);
        }

        for (String choice : annotation.choices()) {
            optionData.addChoice(choice, choice);
        }

        if (type == OptionType.INTEGER || type == OptionType.NUMBER) {
            if (annotation.minValue() != Integer.MIN_VALUE) {
                optionData.setMinValue(annotation.minValue());
            }
            if (annotation.maxValue() != Integer.MAX_VALUE) {
                optionData.setMaxValue(annotation.maxValue());
            }
        }

        if (type == OptionType.STRING) {
            if (annotation.minLength() > 0) {
                optionData.setMinLength(annotation.minLength());
            }

            if (annotation.maxLength() < CommandValidator.MAX_STRING_LENGTH) {
                optionData.setMaxLength(annotation.maxLength());
            }
        }

        return optionData;
    }

    private void setDefaultPermissions(SlashCommandData commandData, String[] defaultMemberPermissions, String commandName) {
        if (defaultMemberPermissions == null || defaultMemberPermissions.length == 0) {
            return;
        }

        if (defaultMemberPermissions.length == 1) {
            handleSinglePermission(commandData, defaultMemberPermissions[0], commandName);
        } else {
            handleMultiplePermissions(commandData, defaultMemberPermissions, commandName);
        }
    }

    private void handleSinglePermission(SlashCommandData commandData, String permission, String commandName) {
        switch (permission) {
            case "DISABLED" -> commandData.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
            case "ENABLED" -> commandData.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
            default -> {
                try {
                    Permission discordPermission = Permission.valueOf(permission);
                    commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(discordPermission));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid permission in defaultMemberPermissions for command '{}': {}", commandName, permission);
                }
            }
        }
    }

    private void handleMultiplePermissions(SlashCommandData commandData, String[] defaultMemberPermissions, String commandName) {
        try {
            Permission[] permissions = new Permission[defaultMemberPermissions.length];
            for (int i = 0; i < defaultMemberPermissions.length; i++) {
                permissions[i] = Permission.valueOf(defaultMemberPermissions[i]);
            }
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid permission in defaultMemberPermissions for command '{}': {}",
                    commandName, String.join(", ", defaultMemberPermissions));
        }
    }
}