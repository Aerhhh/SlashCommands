package net.aerh.slashcommands;

import net.aerh.slashcommands.internal.core.PermissionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How the manager routes interactions: command, button, string-select and modal handlers are
 * dispatched off the event thread, while autocomplete is answered inline. The recording executor
 * captures dispatched tasks without running them, so the {@code null} events are never touched.
 */
class InteractionDispatchTest {

    private SlashCommandManager managerWith(RecordingExecutorService executor) {
        return new SlashCommandManager(new PermissionManager(), executor);
    }

    @Test
    @DisplayName("Slash commands are dispatched off the event thread")
    void slashCommandsAreDispatched() {
        RecordingExecutorService executor = new RecordingExecutorService();
        managerWith(executor).onSlashCommandInteraction(null);
        assertEquals(1, executor.taskCount());
    }

    @Test
    @DisplayName("Button interactions are dispatched off the event thread")
    void buttonInteractionsAreDispatched() {
        RecordingExecutorService executor = new RecordingExecutorService();
        managerWith(executor).onButtonInteraction(null);
        assertEquals(1, executor.taskCount());
    }

    @Test
    @DisplayName("String-select interactions are dispatched off the event thread")
    void stringSelectInteractionsAreDispatched() {
        RecordingExecutorService executor = new RecordingExecutorService();
        managerWith(executor).onStringSelectInteraction(null);
        assertEquals(1, executor.taskCount());
    }

    @Test
    @DisplayName("Modal interactions are dispatched off the event thread")
    void modalInteractionsAreDispatched() {
        RecordingExecutorService executor = new RecordingExecutorService();
        managerWith(executor).onModalInteraction(null);
        assertEquals(1, executor.taskCount());
    }

    @Test
    @DisplayName("Autocomplete is answered inline, not dispatched")
    void autocompleteIsAnsweredInline() {
        RecordingExecutorService executor = new RecordingExecutorService();
        SlashCommandManager manager = managerWith(executor);

        try {
            manager.onCommandAutoCompleteInteraction(null);
        } catch (Throwable ignored) {
            // Handling a null event inline may throw; the point is that nothing was dispatched.
        }

        assertEquals(0, executor.taskCount());
    }

    @Test
    @DisplayName("Shutting down JDA shuts down the dispatch executor")
    void shutdownStopsExecutor() {
        RecordingExecutorService executor = new RecordingExecutorService();
        managerWith(executor).onShutdown(null);
        assertTrue(executor.isShutdown());
    }
}
