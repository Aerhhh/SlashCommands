package net.aerh.slashcommands.internal.registration.registrars;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

/**
 * Interface for registering slash commands with Discord.
 * Implementations handle different registration strategies (global vs guild-specific).
 */
public interface CommandRegistrar {

    /**
     * Registers the provided commands with Discord.
     *
     * @param jda      the JDA instance
     * @param commands the commands to register
     */
    void registerCommands(JDA jda, List<SlashCommandData> commands);

    /**
     * Gets the type of this registrar for logging purposes.
     *
     * @return the registrar type name
     */
    String getType();
}