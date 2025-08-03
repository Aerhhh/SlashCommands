# Discord Slash Command Framework

An annotation-based framework for creating JDA slash commands.

## Installation

This package is available through GitHub Packages. For the most up-to-date installation instructions and version information, visit the [GitHub Packages page](https://github.com/Aerhhh/SlashCommands/packages).

**Maven:**
```xml
<dependency>
    <groupId>net.aerh</groupId>
    <artifactId>slashcommands</artifactId>
    <version>VERSION</version>
</dependency>
```

****Gradle:**
```gradle
implementation 'net.aerh:slashcommands:VERSION'
```

## Features

- Automatic parameter handling from method signatures
- Flexible permission system with custom handlers
- Autocomplete support with pattern matching
- Component interactions (buttons, select menus, modals)
- NSFW filtering and default member permissions
- Comprehensive validation with helpful error messages

## Setup

Set up the command manager:

```java
JDA jda = JDABuilder.createDefault("TOKEN").build();

SlashCommandManager manager = SlashCommandManager.builder()
    .withJDA(jda)
    .registerCommands(new ExampleCommands())
    .scanPackage("com.example.commands") // Optional - scan packages for commands
    .addPermissionHandler(new CustomPermissionHandler()) // Optional - custom permissions
    .build();
```

Optionally, add the `-parameters` compiler flag to automatically infer parameter names as option names:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>VERSION</version>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

## Commands

### Basic Commands

```java
public class ExampleCommands {

    @SlashCommand(name = "ping", description = "Check if bot is online")
    public void ping(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }

    @SlashCommand(name = "greet", description = "Greet someone")
    public void greet(SlashCommandInteractionEvent event, @SlashOption(description = "User to greet") User user) {
        event.reply("Hello " + user.getAsMention() + "!").queue();
    }

    // Subcommand: /admin ban
    @SlashCommand(name = "admin", subcommand = "ban", description = "Ban a user")
    public void ban(SlashCommandInteractionEvent event, @SlashOption(description = "User to ban") User user) { }

    // Subcommand group: /server config set
    @SlashCommand(name = "server", group = "config", subcommand = "set", description = "Set config")
    public void setConfig(SlashCommandInteractionEvent event, @SlashOption(description = "Setting name") String key) { }
}
```

### NSFW and Permission Controls

```java
// NSFW command (only appears in age-restricted channels)
@SlashCommand(name = "adult", description = "Adult content", nsfw = true)
public void adultCommand(SlashCommandInteractionEvent event) { }

// Default member permissions (server admins can override)
@SlashCommand(name = "ban", description = "Ban users", defaultMemberPermissions = {"BAN_MEMBERS"})
public void banUser(SlashCommandInteractionEvent event, @SlashOption(description = "User") User user) { }

// Owner-only command (disabled by default)
@SlashCommand(name = "owner", description = "Owner only", defaultMemberPermissions = {"DISABLED"})
public void ownerCommand(SlashCommandInteractionEvent event) { }
```

## Permissions

**Discord permissions:**
```java
@SlashCommand(name = "kick", description = "Kick a user", requiredPermissions = {"KICK_MEMBERS"})
public void kick(...) { }
```

**Role permissions:**
```java
@SlashCommand(name = "staff", description = "Staff command", requiredPermissions = {"ROLE:Staff"})
public void staffCommand(...) { }
```

**Custom permissions:**
```java
public class CustomPermissionHandler implements PermissionHandler {
    @Override
    public boolean canHandle(String permission) {
        return permission.equals("BOT_OWNER");
    }
    
    @Override
    public PermissionChecker.PermissionResult checkPermission(SlashCommandInteractionEvent event, String permission) {
        return event.getUser().getId().equals("123456789") ? 
            PermissionChecker.PermissionResult.allowed() : 
            PermissionChecker.PermissionResult.denied("Owner only", permission);
    }
    
    @Override
    public int getPriority() {
        return 100; // Higher priority than default handlers
    }
}
```

## Autocomplete

Define handler:
```java
@SlashAutocompleteHandler(id = "item_search")
public List<Command.Choice> searchItems(CommandAutoCompleteInteractionEvent event) {
    String input = event.getFocusedOption().getValue();
    return Stream.of("sword", "shield", "potion")
        .filter(item -> item.startsWith(input))
        .map(item -> new Command.Choice(item, item))
        .collect(Collectors.toList());
}
```

Link to option:
```java
@SlashCommand(name = "give", description = "Give an item")
public void giveItem(SlashCommandInteractionEvent event, @SlashOption(name = "item", description = "Item name", autocompleteId = "item_search") String item) {
    event.reply("Gave " + item + " to " + event.getUser().getAsMention()).queue();
}
```

## Components

**Buttons:**
```java
@SlashCommand(name = "vote", description = "Create a vote")
public void vote(SlashCommandInteractionEvent event) {
    event.reply("Vote now!")
        .addActionRow(
            Button.success("vote_yes", "Yes"),
            Button.danger("vote_no", "No")
        ).queue();
}

@SlashComponentHandler(id = "vote_yes")
public void handleYes(ButtonInteractionEvent event) {
    event.reply("You voted yes!").setEphemeral(true).queue();
}
```

**Pattern matching for dynamic components:**
```java
@SlashComponentHandler(id = "moderation", patterns = {"ban_*", "kick_*", "warn_*"})
public void handleModerationAction(ButtonInteractionEvent event) {
    String componentId = event.getComponentId();
    String userId = componentId.substring(componentId.lastIndexOf('_') + 1);
    event.reply("Processing: " + componentId + " for user " + userId).setEphemeral(true).queue();
}
```

## Modals

Show modal:
```java
@SlashCommand(name = "feedback", description = "Give feedback")
public void feedback(SlashCommandInteractionEvent event) {
    Modal modal = Modal.create("feedback_form", "Feedback")
        .addActionRow(TextInput.create("subject", "Subject", TextInputStyle.SHORT).build())
        .addActionRow(TextInput.create("message", "Message", TextInputStyle.PARAGRAPH).build())
        .build();
    event.replyModal(modal).queue();
}
```

Handle submission:
```java
@SlashModalHandler(id = "feedback_form")
public void handleFeedback(ModalInteractionEvent event, String subject, String message) {
    event.reply("Thanks for the feedback!").setEphemeral(true).queue();
}
```
