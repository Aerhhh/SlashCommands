package net.aerh.slashcommands;

import net.aerh.slashcommands.internal.registration.registrars.GlobalCommandDiff;
import net.aerh.slashcommands.internal.registration.registrars.GlobalCommandDiff.ExistingCommand;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalCommandDiffTest {

    @Test
    @DisplayName("Commands that are still defined are neither stale nor preserved")
    void currentCommandsAreLeftAlone() {
        GlobalCommandDiff diff = GlobalCommandDiff.compute(
                List.of(new ExistingCommand("1", "ping", Command.Type.SLASH)),
                Set.of("ping"));

        assertTrue(diff.getStaleCommands().isEmpty(), "a command that is still defined is not stale");
        assertTrue(diff.getPreservedCommands().isEmpty(), "slash commands are managed, not preserved");
    }

    @Test
    @DisplayName("Slash commands missing from the scan are stale")
    void removedCommandsAreStale() {
        GlobalCommandDiff diff = GlobalCommandDiff.compute(
                List.of(
                        new ExistingCommand("1", "ping", Command.Type.SLASH),
                        new ExistingCommand("2", "gen", Command.Type.SLASH)),
                Set.of("ping"));

        assertEquals(1, diff.getStaleCommands().size());
        assertEquals("gen", diff.getStaleCommands().get(0).name());
        assertEquals("2", diff.getStaleCommands().get(0).id());
    }

    @Test
    @DisplayName("Entry Point commands are preserved and never stale")
    void entryPointCommandsArePreserved() {
        // JDA has no dedicated enum constant for Entry Point commands (type 4) as of 5.6.1, so
        // they surface as UNKNOWN. The diff must preserve them either way.
        GlobalCommandDiff diff = GlobalCommandDiff.compute(
                List.of(new ExistingCommand("1", "launch", Command.Type.UNKNOWN)),
                Set.of());

        assertTrue(diff.getStaleCommands().isEmpty(), "an Entry Point command must never be marked stale");
        assertEquals(1, diff.getPreservedCommands().size());
        assertEquals("launch", diff.getPreservedCommands().get(0).name());
    }

    @Test
    @DisplayName("Context menu commands are preserved, not deleted as stale")
    void contextCommandsArePreserved() {
        GlobalCommandDiff diff = GlobalCommandDiff.compute(
                List.of(
                        new ExistingCommand("1", "Report Message", Command.Type.MESSAGE),
                        new ExistingCommand("2", "View Profile", Command.Type.USER)),
                Set.of("ping"));

        assertTrue(diff.getStaleCommands().isEmpty());
        assertEquals(2, diff.getPreservedCommands().size());
    }

    @Test
    @DisplayName("An empty scan marks every slash command stale but keeps unmanaged commands")
    void emptyScanRemovesOnlySlashCommands() {
        GlobalCommandDiff diff = GlobalCommandDiff.compute(
                List.of(
                        new ExistingCommand("1", "gen", Command.Type.SLASH),
                        new ExistingCommand("2", "launch", Command.Type.UNKNOWN)),
                Set.of());

        assertEquals(1, diff.getStaleCommands().size());
        assertEquals("gen", diff.getStaleCommands().get(0).name());
        assertEquals(1, diff.getPreservedCommands().size());
        assertEquals("launch", diff.getPreservedCommands().get(0).name());
    }

    @Test
    @DisplayName("Nothing registered at Discord yields an empty diff")
    void emptyExistingListYieldsEmptyDiff() {
        GlobalCommandDiff diff = GlobalCommandDiff.compute(List.of(), Set.of("ping"));

        assertTrue(diff.getStaleCommands().isEmpty());
        assertTrue(diff.getPreservedCommands().isEmpty());
    }
}
