package net.aerh.slashcommands.internal.handlers;

import net.aerh.slashcommands.api.permissions.PermissionChecker;
import net.aerh.slashcommands.internal.core.CommandInfo;
import net.aerh.slashcommands.internal.core.PermissionManager;
import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.execution.CommandExecutor;
import net.aerh.slashcommands.internal.execution.ExecutableSlashCommand;
import net.aerh.slashcommands.internal.execution.resolvers.SlashCommandArgumentResolver;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Discord slash command interactions.
 * Manages command execution, permission checking, and error handling.
 */
public class SlashCommandHandler implements InteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandHandler.class);

    private final SlashCommandRegistry registry;
    private final PermissionManager permissionManager;
    private final SlashCommandArgumentResolver argumentResolver;
    private final CommandExecutor commandExecutor;

    public SlashCommandHandler(SlashCommandRegistry registry, PermissionManager permissionManager) {
        this.registry = registry;
        this.permissionManager = permissionManager;
        this.argumentResolver = new SlashCommandArgumentResolver();
        this.commandExecutor = new CommandExecutor();
    }

    /**
     * Handles a slash command interaction event.
     *
     * @param event the slash command interaction event
     */
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String subcommandName = event.getSubcommandName();
        String userId = event.getUser().getId();
        String guildInfo = event.getGuild() != null ? event.getGuild().getName() + " (" + event.getGuild().getId() + ")" : "DM";

        logCommandExecution(commandName, subcommandName, userId, guildInfo);

        try {
            if (subcommandName != null) {
                handleSubcommand(event, commandName, subcommandName);
            } else {
                handleRegularCommand(event, commandName);
            }
        } catch (Exception e) {
            handleCommandError(event, commandName, subcommandName, e);
        }
    }

    private void handleSubcommand(SlashCommandInteractionEvent event, String commandName, String subcommandName) {
        String subcommandGroupName = event.getSubcommandGroup();
        CommandInfo subcommandInfo;

        if (subcommandGroupName != null) {
            subcommandInfo = registry.getSubcommand(commandName, subcommandGroupName, subcommandName);
        } else {
            subcommandInfo = registry.getSubcommand(commandName, subcommandName);
        }

        if (subcommandInfo == null) {
            String fullPath = subcommandGroupName != null ?
                    commandName + " " + subcommandGroupName + " " + subcommandName :
                    commandName + " " + subcommandName;
            logger.error("No handler found for subcommand: {}", fullPath);
            return;
        }

        if (!checkPermissions(event, subcommandInfo)) {
            return;
        }

        executeCommand(subcommandInfo, event);
    }

    private void handleRegularCommand(SlashCommandInteractionEvent event, String commandName) {
        CommandInfo commandInfo = registry.getCommand(commandName);
        if (commandInfo == null) {
            logger.error("No handler found for command: {}", commandName);
            return;
        }

        if (!checkPermissions(event, commandInfo)) {
            return;
        }

        executeCommand(commandInfo, event);
    }

    private boolean checkPermissions(SlashCommandInteractionEvent event, CommandInfo commandInfo) {
        PermissionChecker.PermissionResult permissionResult = permissionManager.checkPermissions(
                event, commandInfo.getAnnotation().requiredPermissions());

        if (!permissionResult.wasSuccessful()) {
            logger.info("Permission denied: {}", permissionResult.getReason());
            event.reply("❌ " + permissionResult.getReason()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    private void executeCommand(CommandInfo commandInfo, SlashCommandInteractionEvent event) {
        Object[] args = argumentResolver.prepareArguments(commandInfo, event);
        ExecutableSlashCommand command = new ExecutableSlashCommand(commandInfo, event, args);

        boolean success = commandExecutor.execute(command, (cmd, error) -> {
            // Custom error handler for slash commands
            if (!event.isAcknowledged()) {
                event.reply("An error occurred while executing the command.").setEphemeral(true).queue();
            }
        });

        if (!success) {
            // Additional error handling if needed
            logger.warn("Command execution failed for: {}", command.getCommandName());
        }
    }

    private void logCommandExecution(String commandName, String subcommandName, String userId, String guildInfo) {
        if (subcommandName != null) {
            logger.info("Subcommand '{} {}' executed by user {} in {}", commandName, subcommandName, userId, guildInfo);
        } else {
            logger.info("Slash command '{}' executed by user {} in {}", commandName, userId, guildInfo);
        }
    }

    private void handleCommandError(SlashCommandInteractionEvent event, String commandName, String subcommandName, Exception e) {
        String fullCommandName = subcommandName != null ? commandName + " " + subcommandName : commandName;
        logger.error("Error executing command '{}': {}", fullCommandName, e.getMessage(), e);

        if (!event.isAcknowledged()) {
            event.reply("An error occurred while executing the command.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getHandlerType() {
        return "SlashCommand";
    }
}