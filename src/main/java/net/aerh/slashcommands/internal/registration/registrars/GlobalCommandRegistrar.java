package net.aerh.slashcommands.internal.registration.registrars;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Registrar for global Discord slash commands.
 * Global commands are available in all servers and DMs where the bot is present.
 */
public class GlobalCommandRegistrar implements CommandRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(GlobalCommandRegistrar.class);

    @Override
    public void registerCommands(JDA jda, List<SlashCommandData> commands) {
        if (commands.isEmpty()) {
            logger.info("No global commands to register");
            return;
        }

        logger.info("Registering {} global commands", commands.size());
        jda.updateCommands().addCommands(commands).queue(
                success -> logger.info("Successfully registered {} global commands", commands.size()),
                error -> logger.error("Failed to register global commands: {}", error.getMessage())
        );
    }

    @Override
    public String getType() {
        return "Global";
    }
}