package net.aerh.slashcommands;

import net.aerh.slashcommands.api.annotations.SlashComponentHandler;
import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentPatternMatchingTest {

    private SlashCommandRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SlashCommandRegistry();
        registry.registerCommands(new TestComponentHandlers());
    }

    @Test
    @DisplayName("Component handler should match exact ID")
    void testExactIdMatching() {
        var componentInfo = registry.getComponentHandler("exact_button");

        assertNotNull(componentInfo, "Should find handler for exact ID match");
        assertEquals("exactHandler", componentInfo.method().getName());
    }

    @Test
    @DisplayName("Component handler should match wildcard patterns")
    void testWildcardPatternMatching() {
        // Test various wildcard patterns
        String[] testIds = {
            "user_123", "user_456", "user_admin",
            "action_ban", "action_kick", "action_warn",
            "temp_delete_abc", "temp_edit_xyz"
        };

        for (String componentId : testIds) {
            var componentInfo = registry.getComponentHandler(componentId);
            
            assertNotNull(componentInfo, "Should find handler for pattern: " + componentId);
            
            // Check which handler was matched based on the pattern
            if (componentId.startsWith("user_")) {
                assertEquals("userActionHandler", componentInfo.method().getName());
            } else if (componentId.startsWith("action_")) {
                assertEquals("moderationHandler", componentInfo.method().getName());
            } else if (componentId.startsWith("temp_")) {
                assertEquals("tempActionHandler", componentInfo.method().getName());
            }
        }
    }

    @Test
    @DisplayName("Component handler should not match non-matching patterns")
    void testNonMatchingPatterns() {
        String[] nonMatchingIds = {
            "different_button",
            "random_id", 
            "use_123", // missing 'r' from user_
            "actions_ban", // extra 's' 
            "temporary_delete" // different prefix
        };

        for (String componentId : nonMatchingIds) {
            var componentInfo = registry.getComponentHandler(componentId);
            
            assertNull(componentInfo, "Should not find handler for non-matching pattern: " + componentId);
        }
    }

    @Test
    @DisplayName("Component handler with both exact ID and patterns should work correctly")
    void testExactIdAndPatternsCoexistence() {
        // Test exact ID match
        var componentInfo = registry.getComponentHandler("combined_handler");
        assertNotNull(componentInfo, "Should find handler for exact ID");
        assertEquals("combinedHandler", componentInfo.method().getName());

        // Test pattern matching still works
        componentInfo = registry.getComponentHandler("pattern_match_test");
        assertNotNull(componentInfo, "Should find handler for pattern match");
        assertEquals("combinedHandler", componentInfo.method().getName());
    }

    @Test
    @DisplayName("Multiple patterns in single handler should all work")
    void testMultiplePatterns() {
        String[] multiPatternIds = {
            "confirm_delete", "confirm_ban", "confirm_kick", // confirm_* pattern
            "cancel_action", "cancel_edit", "cancel_process"  // cancel_* pattern
        };

        for (String componentId : multiPatternIds) {
            var componentInfo = registry.getComponentHandler(componentId);
            
            assertNotNull(componentInfo, "Should find handler for multi-pattern: " + componentId);
            assertEquals("confirmCancelHandler", componentInfo.method().getName());
        }
    }

    public static class TestComponentHandlers {

        @SlashComponentHandler(id = "exact_button")
        public void exactHandler(ButtonInteractionEvent event) {
            // Exact ID match
        }

        @SlashComponentHandler(id = "user_action", patterns = {"user_*"})
        public void userActionHandler(ButtonInteractionEvent event) {
            // Handles user_123, user_456, etc.
        }

        @SlashComponentHandler(id = "moderation", patterns = {"action_*"})
        public void moderationHandler(ButtonInteractionEvent event) {
            // Handles action_ban, action_kick, etc.
        }

        @SlashComponentHandler(id = "temp", patterns = {"temp_*"})
        public void tempActionHandler(ButtonInteractionEvent event) {
            // Handles temp_delete_abc, temp_edit_xyz, etc.
        }

        @SlashComponentHandler(id = "combined_handler", patterns = {"pattern_*"})
        public void combinedHandler(ButtonInteractionEvent event) {
            // Both exact ID and pattern matching
        }

        @SlashComponentHandler(id = "multi_pattern", patterns = {"confirm_*", "cancel_*"})
        public void confirmCancelHandler(ButtonInteractionEvent event) {
            // Multiple patterns in one handler
        }
    }
}