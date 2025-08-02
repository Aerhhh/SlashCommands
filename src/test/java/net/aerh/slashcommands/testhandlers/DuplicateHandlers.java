package net.aerh.slashcommands.testhandlers;

import net.aerh.slashcommands.api.annotations.SlashAutocompleteHandler;
import net.aerh.slashcommands.api.annotations.SlashComponentHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;

public class DuplicateHandlers {

    @SlashAutocompleteHandler(id = "duplicate_test_id")
    public List<Command.Choice> autocomplete1(CommandAutoCompleteInteractionEvent event) {
        return List.of();
    }

    @SlashComponentHandler(id = "duplicate_test_id")
    public void component1(ButtonInteractionEvent event) {
    }
}