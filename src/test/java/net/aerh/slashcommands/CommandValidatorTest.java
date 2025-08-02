package net.aerh.slashcommands;

import net.aerh.slashcommands.api.annotations.SlashCommand;
import net.aerh.slashcommands.api.annotations.SlashOption;
import net.aerh.slashcommands.internal.validation.CommandValidator;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandValidatorTest {

    @Test
    @DisplayName("Valid command with correct SlashCommandInteractionEvent parameter")
    void testValidCommand_WithCorrectEventParameter() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("validCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertTrue(errors.isEmpty(), "Valid command should have no validation errors");
    }

    @Test
    @DisplayName("Command missing SlashCommandInteractionEvent parameter should fail validation")
    void testInvalidCommand_MissingEventParameter() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("noEventParameter", String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command without event parameter should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("First parameter must be SlashCommandInteractionEvent")),
                "Should have error about missing SlashCommandInteractionEvent parameter");
    }

    @Test
    @DisplayName("Command with wrong first parameter type should fail validation")
    void testInvalidCommand_WrongFirstParameter() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("wrongFirstParameter", String.class, SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with wrong first parameter should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("First parameter must be SlashCommandInteractionEvent")),
                "Should have error about wrong first parameter type");
    }

    @Test
    @DisplayName("Command with no parameters should fail validation")
    void testInvalidCommand_NoParameters() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("noParameters");

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with no parameters should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Command methods must have SlashCommandInteractionEvent as the first parameter")),
                "Should have error about missing parameters");
    }

    @Test
    @DisplayName("Command with uppercase characters in name should fail validation")
    void testInvalidCommand_UppercaseName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("uppercaseCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with uppercase name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("lowercase") && e.message().contains("Command name")),
                "Should have error about lowercase requirement");
    }

    @Test
    @DisplayName("Command with blank name should fail validation")
    void testInvalidCommand_BlankName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("blankNameCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with blank name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Command name cannot be empty")),
                "Should have error about empty name");
    }

    @Test
    @DisplayName("Command with name longer than 32 characters should fail validation")
    void testInvalidCommand_TooLongName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("tooLongNameCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with too long name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Command name must be 1-32 characters")),
                "Should have error about name length");
    }

    @Test
    @DisplayName("Command with invalid symbols (@, #) in name should fail validation")
    void testInvalidCommand_InvalidSymbolsInName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("invalidSymbolsCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with invalid symbols should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Command name must be 1-32 characters, lowercase, and contain only letters, numbers, underscores, and hyphens")),
                "Should have error about invalid characters");
    }

    @Test
    @DisplayName("Command with spaces in name should fail validation")
    void testInvalidCommand_SpacesInName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("spacesInNameCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with spaces in name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Command name must be 1-32 characters, lowercase, and contain only letters, numbers, underscores, and hyphens")),
                "Should have error about invalid characters");
    }

    @Test
    @DisplayName("Command with description longer than 100 characters should fail validation")
    void testInvalidCommand_TooLongDescription() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("tooLongDescriptionCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Command with too long description should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Command description must be 100 characters or less")),
                "Should have error about description length");
    }

    @Test
    @DisplayName("Option with blank name should use parameter name and be valid")
    void testValidOption_BlankNameUsesParameterName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("blankOptionNameCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertTrue(errors.isEmpty(), "Option with blank name should use parameter name and be valid");
    }

    @Test
    @DisplayName("Option with name longer than 32 characters should fail validation")
    void testInvalidOption_TooLongName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("tooLongOptionNameCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Option with too long name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Option name must be 1-32 characters")),
                "Should have error about option name length");
    }

    @Test
    @DisplayName("Option with description longer than 100 characters should fail validation")
    void testInvalidOption_TooLongDescription() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("tooLongOptionDescriptionCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Option with too long description should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Option description must be 100 characters or less")),
                "Should have error about option description length");
    }

    @Test
    @DisplayName("Option with minLength greater than maxLength should fail validation")
    void testInvalidOption_InvalidStringLimits() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("invalidStringLimitsCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Option with invalid string limits should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("minLength cannot be greater than maxLength")),
                "Should have error about string length limits");
    }

    @Test
    @DisplayName("Option with more than 25 choices should fail validation")
    void testInvalidOption_TooManyChoices() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("tooManyChoicesCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Option with too many choices should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("cannot have more than 25 choices")),
                "Should have error about too many choices");
    }

    @Test
    @DisplayName("Option with both choices and autocomplete should fail validation")
    void testInvalidOption_ChoicesAndAutocomplete() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("choicesAndAutocompleteCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Option with both choices and autocomplete should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("cannot have both choices and autocomplete")),
                "Should have error about choices and autocomplete conflict");
    }



    @Test
    @DisplayName("Subcommand with invalid group name should fail validation")
    void testInvalidSubcommand_InvalidGroupName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("invalidGroupCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Invalid group name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Subcommand group name must be 1-32 characters")),
                "Should have error about invalid group name");
    }

    @Test
    @DisplayName("Valid subcommand with proper structure should pass validation")
    void testValidSubcommand_ProperStructure() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("validSubcommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertTrue(errors.isEmpty(), "Valid subcommand should have no validation errors");
    }

    @Test
    @DisplayName("Valid subcommand group with proper structure should pass validation")
    void testValidSubcommandGroup_ProperStructure() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("validSubcommandGroup", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertTrue(errors.isEmpty(), "Valid subcommand group should have no validation errors");
    }

    @Test
    @DisplayName("Required option placed after optional option should fail validation")
    void testInvalidOption_RequiredAfterOptional() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("requiredAfterOptionalCommand", SlashCommandInteractionEvent.class, String.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Required option after optional should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Required options must come before optional options")),
                "Should have error about option order");
    }

    @Test
    @DisplayName("Subcommand with invalid subcommand name should fail validation")
    void testInvalidSubcommand_InvalidSubcommandName() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("invalidSubcommandNameCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Invalid subcommand name should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Subcommand name must be 1-32 characters")),
                "Should have error about invalid subcommand name");
    }

    @Test
    @DisplayName("Group without subcommand should fail validation")
    void testInvalidSubcommand_GroupWithoutSubcommand() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("groupWithoutSubcommandCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Group without subcommand should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("requires a subcommand name")),
                "Should have error about missing subcommand");
    }

    @Test
    @DisplayName("Non-public command method should fail validation")
    void testInvalidCommand_NonPublicMethod() throws NoSuchMethodException {
        Method method = TestCommands.class.getDeclaredMethod("privateCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Non-public method should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("must be public")),
                "Should have error about method visibility");
    }

    @Test
    @DisplayName("Static command method should fail validation")
    void testInvalidCommand_StaticMethod() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("staticCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Static method should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("cannot be static")),
                "Should have error about static method");
    }

    @Test
    @DisplayName("Non-void return type should fail validation")
    void testInvalidCommand_NonVoidReturnType() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("nonVoidReturnCommand", SlashCommandInteractionEvent.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Non-void return type should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("must return void")),
                "Should have error about return type");
    }

    @Test
    @DisplayName("Invalid numeric range (min > max) should fail validation")
    void testInvalidOption_InvalidNumericRange() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("invalidNumericRangeCommand", SlashCommandInteractionEvent.class, Integer.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Invalid numeric range should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("minValue cannot be greater than maxValue")),
                "Should have error about numeric range");
    }

    @Test
    @DisplayName("Empty choice should fail validation")
    void testInvalidOption_EmptyChoice() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("emptyChoiceCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Empty choice should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("cannot be empty")),
                "Should have error about empty choice");
    }

    @Test
    @DisplayName("Duplicate choices should fail validation")
    void testInvalidOption_DuplicateChoices() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("duplicateChoicesCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Duplicate choices should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("Duplicate choice")),
                "Should have error about duplicate choices");
    }

    @Test
    @DisplayName("Too long choice should fail validation")
    void testInvalidOption_TooLongChoice() throws NoSuchMethodException {
        Method method = TestCommands.class.getMethod("tooLongChoiceCommand", SlashCommandInteractionEvent.class, String.class);

        List<CommandValidator.ValidationError> errors = CommandValidator.validateCommand(method);

        assertFalse(errors.isEmpty(), "Too long choice should have validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("is too long (max 100 characters)")),
                "Should have error about choice length");
    }

    // Test command classes
    public static class TestCommands {

        @SlashCommand(name = "valid", description = "Valid command")
        public void validCommand(SlashCommandInteractionEvent event, @SlashOption(description = "Test param") String param) {
        }

        @SlashCommand(name = "no-event", description = "Missing event parameter")
        public void noEventParameter(@SlashOption(description = "Test param") String param) {
        }

        @SlashCommand(name = "wrong-order", description = "Wrong parameter order")
        public void wrongFirstParameter(@SlashOption(description = "Test param") String param, SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "no-params", description = "No parameters")
        public void noParameters() {
        }

        @SlashCommand(name = "InvalidName", description = "Uppercase name test")
        public void uppercaseCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "", description = "Blank name test")
        public void blankNameCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "this_command_name_is_way_too_long_and_exceeds_the_thirty_two_character_limit", description = "Too long name test")
        public void tooLongNameCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "invalid@symbols#", description = "Invalid symbols test")
        public void invalidSymbolsCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "spaces in name", description = "Spaces in name test")
        public void spacesInNameCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "toolong", description = "This description is way too long and exceeds the one hundred character limit that Discord enforces for command descriptions which must be under one hundred chars")
        public void tooLongDescriptionCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "blank-option", description = "Test blank option name")
        public void blankOptionNameCommand(SlashCommandInteractionEvent event, @SlashOption(name = "", description = "Test") String param) {
        }

        @SlashCommand(name = "long-option", description = "Test long option name")
        public void tooLongOptionNameCommand(SlashCommandInteractionEvent event, @SlashOption(name = "this_option_name_is_way_too_long_and_exceeds_thirty_two_chars", description = "Test") String param) {
        }

        @SlashCommand(name = "long-desc", description = "Test long option description")
        public void tooLongOptionDescriptionCommand(SlashCommandInteractionEvent event, @SlashOption(description = "This option description is way too long and exceeds the one hundred character limit that Discord enforces for option descriptions") String param) {
        }

        @SlashCommand(name = "string-limits", description = "Test invalid string limits")
        public void invalidStringLimitsCommand(SlashCommandInteractionEvent event, @SlashOption(description = "Test", minLength = 10, maxLength = 5) String param) {
        }

        @SlashCommand(name = "many-choices", description = "Test too many choices")
        public void tooManyChoicesCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Test", choices = {
                    "choice1", "choice2", "choice3", "choice4", "choice5", "choice6", "choice7", "choice8", "choice9", "choice10",
                    "choice11", "choice12", "choice13", "choice14", "choice15", "choice16", "choice17", "choice18", "choice19", "choice20",
                    "choice21", "choice22", "choice23", "choice24", "choice25", "choice26" // 26 choices (exceeds limit of 25)
                }) String param) {
        }

        @SlashCommand(name = "conflict", description = "Test choices and autocomplete conflict")
        public void choicesAndAutocompleteCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Test", choices = {"choice1", "choice2"}, autocompleteId = "test") String param) {
        }



        @SlashCommand(name = "test", group = "Invalid@Group", subcommand = "sub", description = "Invalid group test")
        public void invalidGroupCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "admin", subcommand = "kick", description = "Valid subcommand")
        public void validSubcommand(SlashCommandInteractionEvent event, @SlashOption(description = "User") String user) {
        }

        @SlashCommand(name = "server", group = "config", subcommand = "set", description = "Valid subcommand group")
        public void validSubcommandGroup(SlashCommandInteractionEvent event, @SlashOption(description = "Setting") String setting) {
        }

        @SlashCommand(name = "order", description = "Test option order")
        public void requiredAfterOptionalCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Optional param", required = false) String optional,
                @SlashOption(description = "Required param", required = true) String required) {
        }

        @SlashCommand(name = "test", subcommand = "Invalid@Subcommand", description = "Invalid subcommand name")
        public void invalidSubcommandNameCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "test", group = "config", description = "Group without subcommand")
        public void groupWithoutSubcommandCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "private-test", description = "Private method test")
        private void privateCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "static-test", description = "Static method test")
        public static void staticCommand(SlashCommandInteractionEvent event) {
        }

        @SlashCommand(name = "return-test", description = "Non-void return test")
        public String nonVoidReturnCommand(SlashCommandInteractionEvent event) {
            return "test";
        }

        @SlashCommand(name = "numeric", description = "Invalid numeric range test")
        public void invalidNumericRangeCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Number", minValue = 100, maxValue = 50) Integer number) {
        }

        @SlashCommand(name = "empty-choice", description = "Empty choice test")
        public void emptyChoiceCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Test", choices = {"valid", "", "another"}) String param) {
        }

        @SlashCommand(name = "duplicate", description = "Duplicate choices test")
        public void duplicateChoicesCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Test", choices = {"choice1", "choice2", "choice1"}) String param) {
        }

        @SlashCommand(name = "long-choice", description = "Too long choice test")
        public void tooLongChoiceCommand(SlashCommandInteractionEvent event, 
                @SlashOption(description = "Test", choices = {"This choice is way too long and exceeds the one hundred character limit that Discord enforces for choice values in slash command options"}) String param) {
        }
    }
}