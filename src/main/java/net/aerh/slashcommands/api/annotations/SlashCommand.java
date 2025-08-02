package net.aerh.slashcommands.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a Discord slash command handler.
 *
 * <p>Methods annotated with this annotation will be automatically registered as slash commands
 * when the containing class is registered with a {@link net.aerh.slashcommands.SlashCommandManager}.
 * The framework supports simple commands, subcommands, and subcommand groups with automatic
 * parameter injection and permission checking.
 *
 * <p>Method signature requirements:
 * <ul>
 *   <li>First parameter must be {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent}</li>
 *   <li>Additional parameters represent command options (annotated with {@link SlashOption})</li>
 *   <li>Return type must be void</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple command
 * @SlashCommand(name = "ping", description = "Check bot responsiveness")
 * public void ping(SlashCommandInteractionEvent event) {
 *     event.reply("Pong!").queue();
 * }
 *
 * // Command with options
 * @SlashCommand(name = "greet", description = "Greet a user")
 * public void greet(SlashCommandInteractionEvent event,
 *                   @SlashOption(description = "User to greet") User user,
 *                   @SlashOption(description = "Custom message", required = false) String message) {
 *     String greeting = message != null ? message : "Hello";
 *     event.reply(greeting + " " + user.getAsMention() + "!").queue();
 * }
 *
 * // Subcommand
 * @SlashCommand(name = "admin", subcommand = "kick", description = "Kick a user",
 *              guildOnly = true, requiredPermissions = {"KICK_MEMBERS"})
 * public void adminKick(SlashCommandInteractionEvent event,
 *                       @SlashOption(description = "User to kick") User user,
 *                       @SlashOption(description = "Reason", required = false) String reason) {
 *     // Implementation
 * }
 *
 * // Subcommand group
 * @SlashCommand(name = "server", group = "config", subcommand = "set",
 *              description = "Set server configuration",
 *              requiredPermissions = {"MANAGE_SERVER"})
 * public void setServerConfig(SlashCommandInteractionEvent event,
 *                             @SlashOption(description = "Setting name") String setting,
 *                             @SlashOption(description = "Setting value") String value) {
 *     // Implementation
 * }
 *
 * // NSFW command with custom permissions
 * @SlashCommand(name = "mature", description = "Adult content command",
 *              nsfw = true, requiredPermissions = {"CUSTOM_PERMISSION"})
 * public void matureContent(SlashCommandInteractionEvent event) {
 *     // Implementation
 * }
 * }</pre>
 *
 * @see net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
 * @see SlashOption
 * @see net.aerh.slashcommands.SlashCommandManager
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommand {

    /**
     * The name of the slash command.
     * Must be 1-32 characters, lowercase, and may contain dashes and underscores.
     * For subcommands, this represents the parent command name.
     *
     * @return the command name
     */
    String name();

    /**
     * The description of the slash command shown to users.
     * Must be 1-100 characters.
     *
     * @return the command description
     */
    String description() default "No description provided";

    /**
     * The subcommand group name if this command belongs to a group.
     * Used to create command hierarchies like "/server config set" where "config" is the group.
     * Cannot be used without a subcommand name.
     *
     * @return the subcommand group name, or empty string if not applicable
     */
    String group() default "";

    /**
     * The subcommand name if this is a subcommand.
     * Used to create command hierarchies like "/admin kick" where "kick" is the subcommand.
     * When used with a group, creates three-level commands like "/server config set".
     *
     * @return the subcommand name, or empty string if not applicable
     */
    String subcommand() default "";

    /**
     * Whether this command can only be used in guilds (servers).
     *
     * @return true if guild-only, false if available everywhere
     */
    boolean guildOnly() default false;

    /**
     * Array of required permissions for this command.
     * Supports multiple permission types:
     * <ul>
     *   <li>Discord permissions: "KICK_MEMBERS", "BAN_MEMBERS", "MANAGE_CHANNELS", etc.</li>
     *   <li>Role-based permissions: "ROLE:Admin", "ROLE:Moderator", etc.</li>
     *   <li>Custom permissions: Handled by registered {@link net.aerh.slashcommands.api.permissions.PermissionHandler}s</li>
     * </ul>
     *
     * <p>All permissions must be satisfied for the command to execute.
     * Use {@link #defaultMemberPermissions()} for Discord-level permission defaults.
     *
     * @return array of required permission strings
     */
    String[] requiredPermissions() default {};

    /**
     * Whether this command is NSFW (age-restricted).
     * NSFW commands only appear in age-restricted channels.
     *
     * @return true if NSFW, false otherwise
     */
    boolean nsfw() default false;

    /**
     * Default Discord permissions required to see and use this command.
     * Supports Discord permission names (e.g., "BAN_MEMBERS") and special values:
     * - "DISABLED": Only administrators can see this command by default
     * - "ENABLED": Everyone can use this command (default behaviour)
     *
     * <p>Server moderators can override these defaults in Discord's Server Settings.
     *
     * @return array of default member permission strings
     */
    String[] defaultMemberPermissions() default {};
}