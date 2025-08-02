package net.aerh.slashcommands.internal.registration;

import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.registration.builders.CommandDataBuilder;
import net.aerh.slashcommands.internal.registration.processors.CommandProcessingContext;
import net.aerh.slashcommands.internal.registration.processors.CommandProcessor;
import net.aerh.slashcommands.internal.registration.registrars.CommandRegistrar;
import net.aerh.slashcommands.internal.registration.registrars.GlobalCommandRegistrar;
import net.aerh.slashcommands.internal.registration.registrars.GuildCommandRegistrar;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for registering slash commands with Discord.
 * Coordinates the conversion of framework command definitions to Discord API structures
 * and delegates registration to appropriate registrars.
 */
public class DiscordCommandRegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(DiscordCommandRegistrationService.class);

    private final CommandDataBuilder commandDataBuilder;
    private final CommandRegistrar globalRegistrar;
    private final CommandRegistrar guildRegistrar;
    private final CommandProcessor processor;

    public DiscordCommandRegistrationService() {
        this.commandDataBuilder = new CommandDataBuilder();
        this.globalRegistrar = new GlobalCommandRegistrar();
        this.guildRegistrar = new GuildCommandRegistrar();
        this.processor = new CommandProcessor();
    }


    /**
     * Registers all commands from the registry with Discord.
     *
     * @param jda      the JDA instance
     * @param registry the command registry containing all commands
     */
    public void registerAllCommands(JDA jda, SlashCommandRegistry registry) {
        // Create processing context with all command data
        CommandProcessingContext context = new CommandProcessingContext(
                registry.getAllCommands(),
                registry.getAllSubcommandGroups(),
                registry.getAllSubcommands(),
                commandDataBuilder
        );

        logger.info("Registering {} top-level commands, {} subcommand groups, {} subcommand group sets with JDA",
                context.getAllCommands().size(),
                context.getAllSubcommandGroups().size(),
                context.getAllSubcommands().size());

        // Process all commands
        processAllCommands(context);

        // Register with Discord
        globalRegistrar.registerCommands(jda, context.getGlobalCommands());
        guildRegistrar.registerCommands(jda, context.getGuildOnlyCommands());
    }

    /**
     * Processes all commands using the command processor.
     *
     * @param context the processing context containing all command data
     */
    private void processAllCommands(CommandProcessingContext context) {
        processor.processCommands(context);
    }

}