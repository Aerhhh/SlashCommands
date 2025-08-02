package net.aerh.slashcommands;

import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandlerIdValidationTest {

    @Test
    void testDuplicateHandlerIds_ShouldFail() {
        SlashCommandRegistry registry = new SlashCommandRegistry();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registry.scanAndRegister("net.aerh.slashcommands.testhandlers");
        });

        assertTrue(exception.getMessage().contains("Duplicate handler IDs:"));
        assertTrue(exception.getMessage().contains("duplicate_test_id"));
        assertTrue(exception.getMessage().contains("AutocompleteHandler") && exception.getMessage().contains("ComponentHandler"));
    }

}