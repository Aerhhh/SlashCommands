package net.aerh.slashcommands.internal.registration.registrars;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registrar for global Discord slash commands.
 * Global commands are available in all servers and DMs where the bot is present.
 *
 * <p>Before writing anything, the registrar retrieves the commands currently registered with
 * Discord and diffs them against the commands produced by the scan. When Discord only holds
 * slash commands, a single bulk overwrite replaces them atomically. When Discord also holds
 * commands the framework does not manage, such as an application's Entry Point command, commands
 * are upserted and deleted individually instead: a bulk overwrite would drop the Entry Point
 * command, which Discord rejects with error 50240.
 */
public class GlobalCommandRegistrar implements CommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(GlobalCommandRegistrar.class);

    @Override
    public void registerCommands(JDA jda, List<SlashCommandData> commands) {
        jda.retrieveCommands().queue(
                existing -> reconcile(jda, commands, existing),
                error -> logger.error("Failed to retrieve existing global commands, skipping global command update: {}", error.getMessage())
        );
    }

    private void reconcile(JDA jda, List<SlashCommandData> desired, List<Command> existing) {
        Set<String> desiredNames = desired.stream()
                .map(SlashCommandData::getName)
                .collect(Collectors.toSet());

        List<GlobalCommandDiff.ExistingCommand> existingCommands = existing.stream()
                .map(command -> new GlobalCommandDiff.ExistingCommand(command.getId(), command.getName(), command.getType()))
                .toList();

        GlobalCommandDiff diff = GlobalCommandDiff.compute(existingCommands, desiredNames);

        diff.getStaleCommands().forEach(command ->
                logger.info("Global command '{}' ({}) is no longer defined and will be removed", command.name(), command.id()));
        diff.getPreservedCommands().forEach(command ->
                logger.info("Preserving unmanaged global command '{}' of type {}", command.name(), command.type()));

        if (diff.getPreservedCommands().isEmpty()) {
            bulkReplace(jda, desired);
        } else {
            replaceIndividually(jda, desired, diff.getStaleCommands());
        }
    }

    private void bulkReplace(JDA jda, List<SlashCommandData> desired) {
        if (desired.isEmpty()) {
            logger.info("No global commands configured, clearing any stale global commands");
            jda.updateCommands().queue(
                    success -> logger.info("Successfully cleared all global commands"),
                    error -> logger.error("Failed to clear global commands: {}", error.getMessage())
            );
            return;
        }

        logger.info("Registering {} global command(s)", desired.size());
        jda.updateCommands().addCommands(desired).queue(
                success -> logger.info("Successfully registered {} global command(s)", desired.size()),
                error -> logger.error("Failed to register global commands: {}", error.getMessage())
        );
    }

    private void replaceIndividually(JDA jda, List<SlashCommandData> desired, List<GlobalCommandDiff.ExistingCommand> stale) {
        if (desired.isEmpty() && stale.isEmpty()) {
            logger.info("Global commands are already in sync");
            return;
        }

        if (!desired.isEmpty()) {
            logger.info("Registering {} global command(s) individually to preserve unmanaged commands", desired.size());
        }

        desired.forEach(command -> jda.upsertCommand(command).queue(
                success -> logger.info("Successfully registered global command '{}'", command.getName()),
                error -> logger.error("Failed to register global command '{}': {}", command.getName(), error.getMessage())
        ));

        stale.forEach(command -> jda.deleteCommandById(command.id()).queue(
                success -> logger.info("Successfully removed stale global command '{}'", command.name()),
                error -> logger.error("Failed to remove stale global command '{}': {}", command.name(), error.getMessage())
        ));
    }

    @Override
    public String getType() {
        return "Global";
    }
}
