package net.aerh.slashcommands.examples;

import net.aerh.slashcommands.api.annotations.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.List;
import java.util.stream.Stream;

public class ExampleCommands {

    // ===============================
    // REGULAR COMMANDS
    // ===============================

    @SlashCommand(name = "ping", description = "Check if the bot is responsive")
    public void ping(SlashCommandInteractionEvent event) {
        long ping = event.getJDA().getGatewayPing();
        event.reply("Gateway ping: " + ping + "ms").queue();
    }

    @SlashCommand(name = "echo", description = "Echo back a message")
    public void echo(SlashCommandInteractionEvent event,
                     @SlashOption(description = "Message to echo back") String message,
                     @SlashOption(description = "Whether to make the response ephemeral") boolean ephemeral) {
        event.reply("📢 " + message).setEphemeral(ephemeral).queue();
    }

    @SlashCommand(name = "userinfo", description = "Get information about a user")
    public void userInfo(SlashCommandInteractionEvent event, @SlashOption(description = "User to get info about", required = false) User user) {
        User targetUser = user != null ? user : event.getUser();

        event.reply("👤 **User Info**\n" +
                        "Name: " + targetUser.getEffectiveName() + "\n" +
                        "ID: " + targetUser.getId() + "\n" +
                        "Account Created: <t:" + targetUser.getTimeCreated().toEpochSecond() + ":F>")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "admin", subcommand = "kick", description = "Kick a user from the server", guildOnly = true, requiredPermissions = {"KICK_MEMBERS"})
    public void adminKick(SlashCommandInteractionEvent event,
                          @SlashOption(description = "User to kick") User user,
                          @SlashOption(description = "Reason for kick", required = false) String reason) {
        String kickReason = reason != null ? reason : "No reason provided";
        event.reply("Kicked " + user.getAsMention() + " for: " + kickReason)
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "admin", subcommand = "ban", description = "Ban a user from the server", guildOnly = true, requiredPermissions = {"BAN_MEMBERS"})
    public void adminBan(SlashCommandInteractionEvent event,
                         @SlashOption(description = "User to ban") User user,
                         @SlashOption(description = "Reason for ban", required = false) String reason,
                         @SlashOption(type = OptionType.INTEGER, description = "Days of messages to delete", minValue = 0, maxValue = 7, required = false) Integer deleteMessageDays) {
        String banReason = reason != null ? reason : "No reason provided";
        int deleteDays = deleteMessageDays != null ? deleteMessageDays : 0;

        event.reply("Banned " + user.getAsMention() + " for: " + banReason +
                        " (Deleted " + deleteDays + " days of messages)")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "admin", subcommand = "warn", description = "Warn a user", guildOnly = true, requiredPermissions = {"KICK_MEMBERS"})
    public void adminWarn(SlashCommandInteractionEvent event,
                          @SlashOption(description = "User to warn") User user,
                          @SlashOption(description = "Warning message") String warning) {
        event.reply("Warned " + user.getAsMention() + ": " + warning)
                .addActionRow(
                        Button.success("warn_ack_" + user.getId(), "Acknowledge"),
                        Button.danger("warn_appeal_" + user.getId(), "Appeal")
                ).queue();
    }

    @SlashCommand(name = "server", group = "config", subcommand = "set", description = "Set a server configuration option", guildOnly = true, requiredPermissions = {"MANAGE_SERVER"})
    public void serverConfigSet(SlashCommandInteractionEvent event,
                                @SlashOption(description = "Configuration key", autocompleteId = "config_keys") String key,
                                @SlashOption(description = "Configuration value") String value) {
        event.reply("Set server config `" + key + "` to `" + value + "`")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "server", group = "config", subcommand = "get", description = "Get a server configuration option", guildOnly = true)
    public void serverConfigGet(SlashCommandInteractionEvent event, @SlashOption(description = "Configuration key", autocompleteId = "config_keys") String key) {
        String value = "example_value_for_" + key;
        event.reply("Server config `" + key + "` is set to: `" + value + "`")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "server", group = "config", subcommand = "list", description = "List all server configuration options", guildOnly = true)
    public void serverConfigList(SlashCommandInteractionEvent event) {
        event.reply("""
                        📋 **Server Configuration**
                        ```
                        prefix: !
                        welcome_channel: #general
                        auto_role: @Member
                        log_channel: #mod-log
                        ```""")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "server", group = "logs", subcommand = "view", description = "View server logs", guildOnly = true)
    public void serverLogsView(SlashCommandInteractionEvent event,
                               @SlashOption(description = "Log type", choices = {"moderation", "join_leave", "message_delete", "voice"}) String logType,
                               @SlashOption(type = OptionType.INTEGER, description = "Number of entries to show", minValue = 1, maxValue = 50, required = false) Integer limit) {
        int entryLimit = limit != null ? limit : 10;
        event.reply("**" + logType.toUpperCase() + " LOGS** (Last " + entryLimit + " entries)\n" +
                        "```\n" +
                        "[2024-01-15 10:30] User joined: @NewUser\n" +
                        "[2024-01-15 10:25] Message deleted in #general\n" +
                        "[2024-01-15 10:20] User banned: @BadUser\n" +
                        "```")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "server", group = "logs", subcommand = "clear", description = "Clear server logs", guildOnly = true, requiredPermissions = {"MANAGE_SERVER"})
    public void serverLogsClear(SlashCommandInteractionEvent event, @SlashOption(description = "Log type to clear", choices = {"moderation", "join_leave", "message_delete", "voice", "all"}) String logType) {
        event.reply("Cleared " + logType + " logs")
                .addActionRow(
                        StringSelectMenu.create("log_clear_confirm")
                                .setPlaceholder("Confirm log clearing...")
                                .addOption("Yes, clear " + logType + " logs", "confirm_" + logType, "This action cannot be undone")
                                .addOption("Cancel", "cancel", "Keep the logs")
                                .build()
                ).setEphemeral(true).queue();
    }

    @SlashCommand(name = "music", group = "queue", subcommand = "add", description = "Add a song to the music queue", guildOnly = true)
    public void musicQueueAdd(SlashCommandInteractionEvent event,
                              @SlashOption(description = "Song URL or search query") String query,
                              @SlashOption(description = "Add to front of queue", required = false) Boolean priority) {
        boolean addToFront = priority != null && priority;
        String position = addToFront ? "front" : "end";

        event.reply("🎵 Added to " + position + " of queue: `" + query + "`")
                .addActionRow(
                        Button.secondary("queue_view", "View Queue"),
                        Button.primary("play_now", "Play Now"),
                        Button.danger("queue_remove_last", "Remove")
                ).queue();
    }

    @SlashCommand(name = "music", group = "queue", subcommand = "remove", description = "Remove a song from the queue", guildOnly = true)
    public void musicQueueRemove(SlashCommandInteractionEvent event, @SlashOption(type = OptionType.INTEGER, description = "Position in queue to remove", minValue = 1) Integer position) {
        event.reply("🗑️ Removed song at position " + position + " from queue").queue();
    }

    @SlashCommand(name = "music", group = "queue", subcommand = "clear", description = "Clear the music queue", guildOnly = true)
    public void musicQueueClear(SlashCommandInteractionEvent event) {
        event.reply("🧹 Cleared the music queue")
                .addActionRow(Button.danger("queue_clear_confirm", "Confirm Clear"))
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "music", group = "player", subcommand = "play", description = "Start playing music", guildOnly = true)
    public void musicPlayerPlay(SlashCommandInteractionEvent event) {
        event.reply("▶️ Started playing music")
                .addActionRow(
                        Button.secondary("music_pause", "⏸️"),
                        Button.secondary("music_skip", "⏭️"),
                        Button.secondary("music_stop", "⏹️"),
                        Button.secondary("music_volume", "🔊")
                ).queue();
    }

    @SlashCommand(name = "music", group = "player", subcommand = "pause", description = "Pause music playback", guildOnly = true)
    public void musicPlayerPause(SlashCommandInteractionEvent event) {
        event.reply("⏸️ Paused music playback").queue();
    }

    @SlashCommand(name = "music", group = "player", subcommand = "volume", description = "Set music volume", guildOnly = true, requiredPermissions = {"MUSIC_DJ"})
    public void musicPlayerVolume(SlashCommandInteractionEvent event, @SlashOption(type = OptionType.INTEGER, description = "Volume level (0-100)", minValue = 0, maxValue = 100) Integer volume) {
        event.reply("🔊 Set volume to " + volume + "%").queue();
    }

    // ===============================
    // NSFW COMMAND EXAMPLE
    // ===============================

    @SlashCommand(name = "adult", description = "Adult content command", nsfw = true, guildOnly = true)
    public void adultContent(SlashCommandInteractionEvent event) {
        event.reply("🔞 This command is only available in NSFW channels!")
                .setEphemeral(true).queue();
    }

    // ===============================
    // DEFAULT MEMBER PERMISSIONS EXAMPLES
    // ===============================

    @SlashCommand(name = "ban", description = "Ban a user from the server", guildOnly = true, defaultMemberPermissions = {"BAN_MEMBERS"})
    public void banUser(SlashCommandInteractionEvent event,
                        @SlashOption(description = "User to ban") User user,
                        @SlashOption(description = "Reason for ban", required = false) String reason) {
        String banReason = reason != null ? reason : "No reason provided";
        event.reply("🔨 Would ban " + user.getAsMention() + " for: " + banReason)
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "emergency", description = "Emergency meeting command", guildOnly = true, defaultMemberPermissions = {"MANAGE_CHANNEL", "MODERATE_MEMBERS"})
    public void emergencyMeeting(SlashCommandInteractionEvent event) {
        event.reply("Emergency meeting called!").setEphemeral(true).queue();
    }

    @SlashCommand(name = "adminonly", description = "Admin-only command (disabled for all except admins)", guildOnly = true, defaultMemberPermissions = {"DISABLED"})
    public void adminOnlyCommand(SlashCommandInteractionEvent event) {
        event.reply("This command is only visible to administrators by default").setEphemeral(true).queue();
    }

    // ===============================
    // PERMISSION EXAMPLES
    // ===============================

    @SlashCommand(name = "admin", subcommand = "shutdown", description = "Shutdown the bot", requiredPermissions = {"BOT_OWNER"})
    public void adminShutdown(SlashCommandInteractionEvent event) {
        event.reply("Bot shutdown initiated by owner").setEphemeral(true).queue();
        // Note: Actual shutdown logic would go here
    }

    @SlashCommand(name = "premium", description = "Access premium features", requiredPermissions = {"PREMIUM_USER"})
    public void premium(SlashCommandInteractionEvent event) {
        event.reply("✨ Welcome to premium features! Thank you for your support!").setEphemeral(true).queue();
    }

    @SlashCommand(name = "level", description = "Check your level and stats", requiredPermissions = {"LEVEL_10"})
    public void level(SlashCommandInteractionEvent event) {
        event.reply("""
                        📊 **Level Check**
                        You are level 10 or higher! Here are your advanced stats:
                        🏆 Achievements: 25
                        💎 Rare items: 8
                        ⭐ Bonus XP: +50%""")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "veteran", description = "Veteran member benefits", guildOnly = true, requiredPermissions = {"TRUSTED_USER"})
    public void veteran(SlashCommandInteractionEvent event) {
        event.reply("""
                        🎖️ **Veteran Member Benefits**
                        Thank you for being a long-standing member of our community!
                        • Priority support
                        • Special veteran role
                        • Access to exclusive channels""")
                .setEphemeral(true).queue();
    }

    @SlashCommand(name = "fun", subcommand = "8ball", description = "Ask the magic 8-ball a question")
    public void fun8Ball(SlashCommandInteractionEvent event, @SlashOption(description = "Your question for the magic 8-ball") String question) {
        String[] responses = {
                "🎱 It is certain", "🎱 Reply hazy, try again", "🎱 Don't count on it",
                "🎱 It is decidedly so", "🎱 Ask again later", "🎱 My reply is no",
                "🎱 Without a doubt", "🎱 Better not tell you now", "🎱 My sources say no",
                "🎱 Yes definitely", "🎱 Cannot predict now", "🎱 Outlook not so good",
                "🎱 You may rely on it", "🎱 Concentrate and ask again", "🎱 Very doubtful"
        };

        String response = responses[(int) (Math.random() * responses.length)];
        event.reply("**Question:** " + question + "\n**Answer:** " + response).queue();
    }

    @SlashCommand(name = "fun", group = "games", subcommand = "rps", description = "Play Rock Paper Scissors")
    public void funGamesRPS(SlashCommandInteractionEvent event, @SlashOption(description = "Your choice", choices = {"rock", "paper", "scissors"}) String choice) {
        String[] choices = {"rock", "paper", "scissors"};
        String botChoice = choices[(int) (Math.random() * choices.length)];

        String result;
        if (choice.equals(botChoice)) {
            result = "🤝 It's a tie!";
        } else if ((choice.equals("rock") && botChoice.equals("scissors")) ||
                (choice.equals("paper") && botChoice.equals("rock")) ||
                (choice.equals("scissors") && botChoice.equals("paper"))) {
            result = "🎉 You win!";
        } else {
            result = "😅 I win!";
        }

        event.reply("🪨📄✂️ **Rock Paper Scissors**\n" +
                "You chose: **" + choice + "**\n" +
                "I chose: **" + botChoice + "**\n" +
                result).queue();
    }

    // ===============================
    // AUTOCOMPLETE HANDLERS
    // ===============================

    @SlashAutocompleteHandler(id = "config_keys")
    public List<Command.Choice> autocompleteConfigKeys(CommandAutoCompleteInteractionEvent event) {
        String input = event.getFocusedOption().getValue().toLowerCase();

        return Stream.of(
                        "prefix",
                        "welcome_channel",
                        "auto_role",
                        "log_channel",
                        "moderation_role",
                        "mute_role",
                        "verification_level"
                )
                .filter(key -> key.startsWith(input))
                .map(key -> new Command.Choice(key, key))
                .limit(25)
                .toList();
    }

    // ===============================
    // MODAL DIALOGUE EXAMPLES
    // ===============================

    @SlashCommand(name = "feedback", description = "Submit feedback to the developers")
    public void feedback(SlashCommandInteractionEvent event) {
        TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
                .setPlaceholder("Brief description of your feedback")
                .setMinLength(1)
                .setMaxLength(100)
                .setRequired(true)
                .build();

        TextInput details = TextInput.create("details", "Details", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Detailed feedback...")
                .setMinLength(10)
                .setMaxLength(1000)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("feedback_modal", "Submit Feedback")
                .addActionRow(subject)
                .addActionRow(details)
                .build();

        event.replyModal(modal).queue();
    }

    @SlashCommand(name = "report", description = "Report a user or issue", guildOnly = true)
    public void report(SlashCommandInteractionEvent event, @SlashOption(description = "User to report", required = false) User user) {
        String modalId = user != null ? "user_report_" + user.getId() : "general_report";
        String title = user != null ? "Report User: " + user.getEffectiveName() : "Submit Report";

        TextInput category = TextInput.create("category", "Category", TextInputStyle.SHORT)
                .setPlaceholder("e.g., Harassment, Spam, Inappropriate Content")
                .setMinLength(3)
                .setMaxLength(50)
                .setRequired(true)
                .build();

        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Please provide details about the issue...")
                .setMinLength(20)
                .setMaxLength(2000)
                .setRequired(true)
                .build();

        TextInput evidence = TextInput.create("evidence", "Evidence/Links", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Any evidence, message links, or additional information (optional)")
                .setMaxLength(500)
                .setRequired(false)
                .build();

        Modal modal = Modal.create(modalId, title)
                .addActionRow(category)
                .addActionRow(description)
                .addActionRow(evidence)
                .build();

        event.replyModal(modal).queue();
    }

    // ===============================
    // MODAL HANDLERS
    // ===============================

    @SlashModalHandler(id = "feedback_modal")
    public void handleFeedback(ModalInteractionEvent event, String subject, String details) {
        event.reply("📝 **Feedback Submitted**\n" +
                        "**Subject:** " + subject + "\n" +
                        "**Details:** " + details + "\n\n" +
                        "Thank you for your feedback! Our team will review it.")
                .setEphemeral(true).queue();
    }

    @SlashModalHandler(id = "user_report", patterns = {"user_report_*"})
    public void handleUserReport(ModalInteractionEvent event, String category, String description, String evidence) {
        String userId = event.getModalId().replace("user_report_", "");

        event.reply("🚨 **User Report Submitted**\n" +
                        "**Reported User:** <@" + userId + ">\n" +
                        "**Category:** " + category + "\n" +
                        "**Description:** " + description + "\n" +
                        (evidence != null && !evidence.trim().isEmpty() ? "**Evidence:** " + evidence + "\n" : "") +
                        "\nModerators have been notified and will investigate.")
                .setEphemeral(true).queue();
    }

    @SlashModalHandler(id = "general_report")
    public void handleGeneralReport(ModalInteractionEvent event, String category, String description, String evidence) {
        event.reply("📋 **Report Submitted**\n" +
                        "**Category:** " + category + "\n" +
                        "**Description:** " + description + "\n" +
                        (evidence != null && !evidence.trim().isEmpty() ? "**Evidence:** " + evidence + "\n" : "") +
                        "\nModerators have been notified.")
                .setEphemeral(true).queue();
    }

    // ===============================
    // COMPONENT HANDLERS
    // ===============================

    @SlashComponentHandler(id = "warn_ack", patterns = {"warn_ack_*"})
    public void handleWarnAcknowledge(ButtonInteractionEvent event) {
        String userId = event.getComponentId().replace("warn_ack_", "");
        event.reply("✅ Warning acknowledged by <@" + userId + ">").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "warn_appeal", patterns = {"warn_appeal_*"})
    public void handleWarnAppeal(ButtonInteractionEvent event) {
        String userId = event.getComponentId().replace("warn_appeal_", "");
        event.reply("📝 Warning appeal submitted by <@" + userId + ">. Staff will review this.").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "log_clear_confirm")
    public void handleLogClearConfirm(StringSelectInteractionEvent event) {
        String selected = event.getValues().get(0);

        if (selected.startsWith("confirm_")) {
            String logType = selected.replace("confirm_", "");
            event.reply("✅ Successfully cleared " + logType + " logs.").setEphemeral(true).queue();
        } else {
            event.reply("❌ Log clearing cancelled.").setEphemeral(true).queue();
        }
    }

    @SlashComponentHandler(id = "queue_view")
    public void handleQueueView(ButtonInteractionEvent event) {
        event.reply("""
                        🎵 **Current Queue:**
                        1. Song One - Artist A
                        2. Song Two - Artist B
                        3. Song Three - Artist C""")
                .setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "play_now")
    public void handlePlayNow(ButtonInteractionEvent event) {
        event.reply("▶️ Playing song immediately!").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "queue_remove_last")
    public void handleQueueRemoveLast(ButtonInteractionEvent event) {
        event.reply("🗑️ Removed last added song from queue").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "music_pause")
    public void handleMusicPause(ButtonInteractionEvent event) {
        event.reply("⏸️ Music paused").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "music_skip")
    public void handleMusicSkip(ButtonInteractionEvent event) {
        event.reply("⏭️ Skipped to next song").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "music_stop")
    public void handleMusicStop(ButtonInteractionEvent event) {
        event.reply("⏹️ Music stopped and queue cleared").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "music_volume")
    public void handleMusicVolume(ButtonInteractionEvent event) {
        event.reply("🔊 Use `/music player volume` to adjust volume").setEphemeral(true).queue();
    }

    @SlashComponentHandler(id = "queue_clear_confirm")
    public void handleQueueClearConfirm(ButtonInteractionEvent event) {
        event.reply("✅ Music queue cleared successfully!").setEphemeral(true).queue();
    }
}