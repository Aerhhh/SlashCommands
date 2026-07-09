package net.aerh.slashcommands.internal.registration.registrars;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.Set;

/**
 * Computes the difference between the global commands currently registered with Discord and the
 * commands produced by the framework's scan.
 *
 * <p>Only slash commands are considered managed by the framework. Anything else Discord returns,
 * such as an application's Entry Point command, is reported as preserved and must not be removed:
 * Discord rejects any bulk overwrite that would delete an Entry Point command (error 50240).
 * Unknown command types are also preserved so that newer Discord features are never clobbered by
 * an older version of this library.
 */
public final class GlobalCommandDiff {

    private final List<ExistingCommand> staleCommands;
    private final List<ExistingCommand> preservedCommands;

    private GlobalCommandDiff(List<ExistingCommand> staleCommands, List<ExistingCommand> preservedCommands) {
        this.staleCommands = staleCommands;
        this.preservedCommands = preservedCommands;
    }

    /**
     * Diffs the commands registered with Discord against the command names the framework is about
     * to register.
     *
     * @param existing     the commands currently registered with Discord
     * @param desiredNames the names of the slash commands produced by the scan
     * @return the computed diff
     */
    public static GlobalCommandDiff compute(List<ExistingCommand> existing, Set<String> desiredNames) {
        List<ExistingCommand> stale = existing.stream()
                .filter(command -> command.type() == Command.Type.SLASH)
                .filter(command -> !desiredNames.contains(command.name()))
                .toList();

        List<ExistingCommand> preserved = existing.stream()
                .filter(command -> command.type() != Command.Type.SLASH)
                .toList();

        return new GlobalCommandDiff(stale, preserved);
    }

    /**
     * Slash commands registered with Discord that the framework no longer defines. These should
     * be removed.
     *
     * @return the stale slash commands
     */
    public List<ExistingCommand> getStaleCommands() {
        return staleCommands;
    }

    /**
     * Commands the framework does not manage, such as an application's Entry Point command. Their
     * presence means a bulk overwrite cannot be used, because it would delete them.
     *
     * @return the preserved commands
     */
    public List<ExistingCommand> getPreservedCommands() {
        return preservedCommands;
    }

    /**
     * A minimal view of a command registered with Discord, as needed for diffing.
     *
     * @param id   the command's snowflake ID
     * @param name the command name
     * @param type the command type
     */
    public record ExistingCommand(String id, String name, Command.Type type) {
    }
}
