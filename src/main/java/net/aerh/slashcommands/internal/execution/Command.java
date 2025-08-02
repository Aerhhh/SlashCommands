package net.aerh.slashcommands.internal.execution;

/**
 * Interface representing an executable command.
 * Implements the Command pattern for better encapsulation and testing.
 */
public interface Command {

    /**
     * Executes the command.
     *
     * @throws Exception if command execution fails
     */
    void execute() throws Exception;

    /**
     * Gets the name of this command for logging purposes.
     *
     * @return the command name
     */
    String getCommandName();

    /**
     * Gets additional context information about this command execution.
     *
     * @return context information (user, guild, etc.)
     */
    default String getExecutionContext() {
        return "Unknown context";
    }
}