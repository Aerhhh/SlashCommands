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
        if (commands.isEmpty()) {
            logger.info("No guild-only commands to register");
            return;
        }

        List<Guild> guilds = jda.getGuilds();
        logger.info("Registering {} guild-only commands in all {} guilds", commands.size(), guilds.size());

        if (guilds.isEmpty()) {
            logger.warn("No guilds found to register commands in. Commands will not be registered.");
            return;
        }

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