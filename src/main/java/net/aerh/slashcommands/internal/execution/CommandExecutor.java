package net.aerh.slashcommands.internal.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for commands that provides centralised execution logic.
 * Handles logging, error management, and potential middleware integration.
 */
public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    /**
     * Executes a command with proper logging and error handling.
     *
     * @param command the command to execute
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(Command command) {
        logger.info("Executing command: {} ({})", command.getCommandName(), command.getExecutionContext());

        try {
            command.execute();
            logger.debug("Successfully executed command: {}", command.getCommandName());
            return true;
        } catch (Exception e) {
            logger.error("Error executing command '{}': {}", command.getCommandName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Executes a command with a custom error handler.
     *
     * @param command      the command to execute
     * @param errorHandler the error handler to call if execution fails
     * @return true if execution was successful, false otherwise
     */
    public boolean execute(Command command, CommandErrorHandler errorHandler) {
        logger.info("Executing command: {} ({})", command.getCommandName(), command.getExecutionContext());

        try {
            command.execute();
            logger.debug("Successfully executed command: {}", command.getCommandName());
            return true;
        } catch (Exception e) {
            logger.error("Error executing command '{}': {}", command.getCommandName(), e.getMessage(), e);
            errorHandler.handleError(command, e);
            return false;
        }
    }

    /**
     * Interface for handling command execution errors.
     */
    @FunctionalInterface
    public interface CommandErrorHandler {
        void handleError(Command command, Exception error);
    }
}