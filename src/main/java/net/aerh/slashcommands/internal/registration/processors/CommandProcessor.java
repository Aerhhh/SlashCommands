package net.aerh.slashcommands.internal.registration.processors;

import net.aerh.slashcommands.internal.core.CommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Processes all types of commands: regular commands, commands with subcommands,
 * and commands with subcommand groups.
 * <p>
 * This handles all command processing logic in a single, maintainable class
 * that processes commands in the correct order.
 */
public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    /**
     * Processes all commands from the context in the correct order.
     *
     * @param context the processing context containing all command data
     */
    public void processCommands(CommandProcessingContext context) {
        logger.info("Processing all commands with processor");

        processRegularCommands(context);
        processCommandsWithSubcommands(context);
        processCommandsWithSubcommandGroups(context);

        logger.info("Completed processing {} global commands and {} guild-only commands",
                context.getGlobalCommands().size(), context.getGuildOnlyCommands().size());
    }


    /**
     * Processes regular commands that don't have any subcommands or groups.
     */
    private void processRegularCommands(CommandProcessingContext context) {
        logger.debug("Processing regular commands");

        for (Map.Entry<String, CommandInfo> entry : context.getAllCommands().entrySet()) {
            String commandName = entry.getKey();
            CommandInfo commandInfo = entry.getValue();

            if (context.hasSubcommands(commandName)) {
                continue;
            }

            SlashCommandData commandData = context.createSimpleCommand(commandName, commandInfo);
            context.processAndAddCommand(commandName, commandData);
        }
    }

    /**
     * Processes commands that have direct subcommands but no subcommand groups.
     */
    private void processCommandsWithSubcommands(CommandProcessingContext context) {
        logger.debug("Processing commands with direct subcommands");

        for (String commandName : context.getAllSubcommandGroups().keySet()) {
            if (context.isProcessed(commandName)) {
                continue;
            }

            // Skip if this command has subcommand groups - we'll handle it later
            if (context.getAllSubcommands().containsKey(commandName)) {
                continue;
            }

            SlashCommandData commandData = context.createCommandWithSubcommands(commandName);
            context.processAndAddCommand(commandName, commandData);
        }
    }

    /**
     * Processes commands that have subcommand groups (the most complex case).
     */
    private void processCommandsWithSubcommandGroups(CommandProcessingContext context) {
        logger.debug("Processing commands with subcommand groups");

        for (String commandName : context.getAllSubcommands().keySet()) {
            if (context.isProcessed(commandName)) {
                continue;
            }

            SlashCommandData commandData = context.createCommandWithGroups(commandName);
            context.processAndAddCommand(commandName, commandData);
        }
    }
}