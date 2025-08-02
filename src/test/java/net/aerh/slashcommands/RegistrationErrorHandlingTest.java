package net.aerh.slashcommands;

import net.aerh.slashcommands.api.annotations.SlashCommand;
import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RegistrationErrorHandlingTest {

    @Test
    @DisplayName("Registration should continue despite invalid commands")
    void testRegistrationContinuesWithErrors() {
        SlashCommandRegistry registry = new SlashCommandRegistry();
        
        assertDoesNotThrow(() -> {
            registry.registerCommands(new Commands());
        }, "Registration should continue even when some commands have validation errors");
    }

    public static class Commands {

        @SlashCommand(name = "valid", description = "This is a valid command")
        public void validCommand(SlashCommandInteractionEvent event) {
            // Valid command
        }

        @SlashCommand(name = "Invalid@Name", description = "Command with invalid name")
        public void invalidNameCommand(SlashCommandInteractionEvent event) {
            // Invalid command - should be skipped
        }

        @SlashCommand(name = "another-valid", description = "Another valid command")
        public void anotherValidCommand(SlashCommandInteractionEvent event) {
            // Valid command
        }

        @SlashCommand(name = "", description = "Command with blank name")
        public void blankNameCommand(SlashCommandInteractionEvent event) {
            // Invalid command - should be skipped
        }

        @SlashCommand(name = "final-valid", description = "Final valid command")
        public void finalValidCommand(SlashCommandInteractionEvent event) {
            // Valid command
        }
    }
}