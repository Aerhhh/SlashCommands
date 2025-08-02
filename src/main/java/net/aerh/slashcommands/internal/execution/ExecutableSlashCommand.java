package net.aerh.slashcommands.internal.execution;

import net.aerh.slashcommands.internal.core.CommandInfo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Command implementation for Discord slash commands.
 * Encapsulates the execution of a slash command method with proper error handling.
 */
public class ExecutableSlashCommand implements Command {
    private final CommandInfo commandInfo;
    private final Object[] arguments;
    private final String commandName;
    private final String executionContext;

    public ExecutableSlashCommand(CommandInfo commandInfo, SlashCommandInteractionEvent event, Object[] arguments) {
        this.commandInfo = commandInfo;
        this.arguments = arguments;
        this.commandName = buildCommandName(event);
        this.executionContext = buildExecutionContext(event);
    }

    @Override
    public void execute() throws Exception {
        commandInfo.getMethod().invoke(commandInfo.getInstance(), arguments);
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getExecutionContext() {
        return executionContext;
    }

    private String buildCommandName(SlashCommandInteractionEvent event) {
        StringBuilder name = new StringBuilder(event.getName());

        if (event.getSubcommandGroup() != null) {
            name.append(" ").append(event.getSubcommandGroup());
        }

        if (event.getSubcommandName() != null) {
            name.append(" ").append(event.getSubcommandName());
        }

        return name.toString();
    }

    private String buildExecutionContext(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String guildInfo = event.getGuild() != null ?
                event.getGuild().getName() + " (" + event.getGuild().getId() + ")" : "DM";
        return String.format("User: %s, Guild: %s", userId, guildInfo);
    }
}