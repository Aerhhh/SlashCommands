package net.aerh.slashcommands.internal.registration.registrars;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Registrar for guild-specific Discord slash commands.
 * Guild commands are only available in the specific servers where they are registered.
 */
public class GuildCommandRegistrar implements CommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(GuildCommandRegistrar.class);

    @Override
    public void registerCommands(JDA jda, List<SlashCommandData> commands) {
        List<Guild> guilds = jda.getGuilds();

        if (guilds.isEmpty()) {
            logger.warn("No guilds found to register commands in. Commands will not be registered.");
            return;
        }

        if (commands.isEmpty()) {
            logger.info("No guild commands configured, clearing any stale guild commands in {} guild(s)", guilds.size());
            for (Guild guild : guilds) {
                guild.updateCommands().queue(
                        success -> logger.info("Successfully cleared all guild commands in {}", guild.getName()),
                        error -> logger.error("Failed to clear guild commands in {}: {}", guild.getName(), error.getMessage())
                );
            }
            return;
        }

        logger.info("Registering {} guild-only command(s) in {} guild(s)", commands.size(), guilds.size());
        StringBuilder tree = new StringBuilder("Guild command tree:");
        for (SlashCommandData cmd : commands) {
            tree.append("\n/").append(cmd.getName());
            if (!cmd.getDescription().isEmpty()) {
                tree.append(" - ").append(cmd.getDescription());
            }
            for (var sub : cmd.getSubcommands()) {
                tree.append("\n  ├── ").append(sub.getName());
                for (var opt : sub.getOptions()) {
                    tree.append("\n  │   ├── ").append(opt.getName())
                            .append(opt.isRequired() ? " (required)" : "");
                }
            }
            for (var group : cmd.getSubcommandGroups()) {
                tree.append("\n  ├── [").append(group.getName()).append("]");
                for (var sub : group.getSubcommands()) {
                    tree.append("\n  │   ├── ").append(sub.getName());
                    for (var opt : sub.getOptions()) {
                        tree.append("\n  │   │   ├── ").append(opt.getName())
                                .append(opt.isRequired() ? " (required)" : "");
                    }
                }
            }
            for (var opt : cmd.getOptions()) {
                tree.append("\n  ├── ").append(opt.getName())
                        .append(opt.isRequired() ? " (required)" : "");
            }
        }
        logger.info("{}", tree);
        for (Guild guild : guilds) {
            guild.updateCommands().addCommands(commands).queue(
                    success -> logger.info("Successfully registered {} guild-only commands in {}", commands.size(), guild.getName()),
                    error -> logger.error("Failed to register commands in {}: {}", guild.getName(), error.getMessage())
            );
        }
    }

    @Override
    public String getType() {
        return "Guild";
    }
}