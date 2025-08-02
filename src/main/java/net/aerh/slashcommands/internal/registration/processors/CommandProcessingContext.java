package net.aerh.slashcommands.internal.registration.processors;

import net.aerh.slashcommands.internal.core.CommandInfo;
import net.aerh.slashcommands.internal.registration.builders.CommandDataBuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.*;

/**
 * Context object that holds all the data needed for command processing.
 * This reduces parameter passing and makes the code more maintainable.
 */
public class CommandProcessingContext {
    private final Map<String, CommandInfo> allCommands;
    private final Map<String, Map<String, CommandInfo>> allSubcommandGroups;
    private final Map<String, Map<String, Map<String, CommandInfo>>> allSubcommands;
    private final List<SlashCommandData> globalCommands;
    private final List<SlashCommandData> guildOnlyCommands;
    private final CommandDataBuilder commandDataBuilder;
    private final Set<String> processedCommands;

    public CommandProcessingContext(Map<String, CommandInfo> allCommands,
                                    Map<String, Map<String, CommandInfo>> allSubcommandGroups,
                                    Map<String, Map<String, Map<String, CommandInfo>>> allSubcommands,
                                    CommandDataBuilder commandDataBuilder) {
        this.allCommands = allCommands;
        this.allSubcommandGroups = allSubcommandGroups;
        this.allSubcommands = allSubcommands;
        this.commandDataBuilder = commandDataBuilder;
        this.globalCommands = new ArrayList<>();
        this.guildOnlyCommands = new ArrayList<>();
        this.processedCommands = new HashSet<>();
    }

    public Map<String, CommandInfo> getAllCommands() {
        return allCommands;
    }

    public Map<String, Map<String, CommandInfo>> getAllSubcommandGroups() {
        return allSubcommandGroups;
    }

    public Map<String, Map<String, Map<String, CommandInfo>>> getAllSubcommands() {
        return allSubcommands;
    }

    public List<SlashCommandData> getGlobalCommands() {
        return globalCommands;
    }

    public List<SlashCommandData> getGuildOnlyCommands() {
        return guildOnlyCommands;
    }

    public CommandDataBuilder getCommandDataBuilder() {
        return commandDataBuilder;
    }

    public Set<String> getProcessedCommands() {
        return processedCommands;
    }

    public boolean hasSubcommands(String commandName) {
        return allSubcommandGroups.containsKey(commandName) || allSubcommands.containsKey(commandName);
    }

    public boolean hasRegularCommand(String commandName) {
        return allCommands.containsKey(commandName);
    }

    public void addCommand(SlashCommandData commandData, boolean guildOnly) {
        if (guildOnly) {
            guildOnlyCommands.add(commandData);
        } else {
            globalCommands.add(commandData);
        }
    }

    public void markAsProcessed(String commandName) {
        processedCommands.add(commandName);
    }

    public boolean isProcessed(String commandName) {
        return processedCommands.contains(commandName);
    }

    // ===== Enhanced Helper Methods to Reduce Parameter Passing =====

    /**
     * Gets the direct subcommands for a command name.
     */
    public Map<String, CommandInfo> getDirectSubcommands(String commandName) {
        return allSubcommandGroups.get(commandName);
    }

    /**
     * Gets the subcommand groups for a command name.
     */
    public Map<String, Map<String, CommandInfo>> getSubcommandGroups(String commandName) {
        return allSubcommands.get(commandName);
    }

    /**
     * Determines the base command for a command name, checking regular commands first,
     * then falling back to the first available subcommand.
     */
    public CommandInfo determineBaseCommand(String commandName) {
        // Try regular command first
        if (hasRegularCommand(commandName)) {
            return allCommands.get(commandName);
        }

        // Try direct subcommands
        Map<String, CommandInfo> directSubcommands = getDirectSubcommands(commandName);
        if (directSubcommands != null && !directSubcommands.isEmpty()) {
            return directSubcommands.values().iterator().next();
        }

        // Fall back to subcommand groups
        Map<String, Map<String, CommandInfo>> groups = getSubcommandGroups(commandName);
        if (groups != null && !groups.isEmpty()) {
            return groups.values().iterator().next().values().iterator().next();
        }

        throw new IllegalStateException("No base command found for: " + commandName);
    }

    /**
     * Creates a simple command data structure using the context's builder.
     */
    public SlashCommandData createSimpleCommand(String commandName, CommandInfo commandInfo) {
        SlashCommandData commandData = commandDataBuilder.createSlashCommandData(
                commandName,
                commandInfo.getAnnotation().description(),
                commandInfo.getAnnotation().nsfw(),
                commandInfo.getAnnotation().defaultMemberPermissions()
        );

        commandDataBuilder.addOptionsToCommand(commandData, commandInfo);
        return commandData;
    }

    /**
     * Creates command data for commands with direct subcommands.
     */
    public SlashCommandData createCommandWithSubcommands(String commandName) {
        Map<String, CommandInfo> directSubcommands = getDirectSubcommands(commandName);
        CommandInfo baseCommand = determineBaseCommand(commandName);

        String description = hasRegularCommand(commandName)
                ? baseCommand.getAnnotation().description()
                : "Commands for " + commandName;

        SlashCommandData commandData = commandDataBuilder.createSlashCommandData(
                commandName,
                description,
                baseCommand.getAnnotation().nsfw(),
                baseCommand.getAnnotation().defaultMemberPermissions()
        );

        // Add all direct subcommands
        for (CommandInfo subcommandInfo : directSubcommands.values()) {
            commandData.addSubcommands(commandDataBuilder.createSubcommandData(subcommandInfo));
        }

        return commandData;
    }

    /**
     * Creates command data for commands with subcommand groups.
     */
    public SlashCommandData createCommandWithGroups(String commandName) {
        Map<String, Map<String, CommandInfo>> groups = getSubcommandGroups(commandName);
        Map<String, CommandInfo> directSubcommands = getDirectSubcommands(commandName);
        CommandInfo baseCommand = determineBaseCommand(commandName);

        String description = hasRegularCommand(commandName)
                ? baseCommand.getAnnotation().description()
                : "Commands for " + commandName;

        SlashCommandData commandData = commandDataBuilder.createSlashCommandData(
                commandName,
                description,
                baseCommand.getAnnotation().nsfw(),
                baseCommand.getAnnotation().defaultMemberPermissions()
        );

        // Add direct subcommands first (if any)
        if (directSubcommands != null) {
            for (CommandInfo subcommandInfo : directSubcommands.values()) {
                commandData.addSubcommands(commandDataBuilder.createSubcommandData(subcommandInfo));
            }
        }

        // Add subcommand groups
        for (Map.Entry<String, Map<String, CommandInfo>> groupEntry : groups.entrySet()) {
            String groupName = groupEntry.getKey();
            Map<String, CommandInfo> subcommands = groupEntry.getValue();

            var groupData = commandDataBuilder.createSubcommandGroupData(groupName, "Group: " + groupName);

            for (CommandInfo subcommandInfo : subcommands.values()) {
                groupData.addSubcommands(commandDataBuilder.createSubcommandData(subcommandInfo));
            }

            commandData.addSubcommandGroups(groupData);
        }

        return commandData;
    }

    /**
     * Processes and adds a command to the appropriate list, then marks it as processed.
     */
    public void processAndAddCommand(String commandName, SlashCommandData commandData) {
        CommandInfo baseCommand = determineBaseCommand(commandName);
        addCommand(commandData, baseCommand.getAnnotation().guildOnly());
        markAsProcessed(commandName);
    }
}