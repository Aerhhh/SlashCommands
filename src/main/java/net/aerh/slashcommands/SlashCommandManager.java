package net.aerh.slashcommands;

import net.aerh.slashcommands.api.permissions.PermissionHandler;
import net.aerh.slashcommands.internal.core.PermissionManager;
import net.aerh.slashcommands.internal.core.SlashCommandRegistry;
import net.aerh.slashcommands.internal.handlers.AutocompleteHandler;
import net.aerh.slashcommands.internal.handlers.ComponentInteractionHandler;
import net.aerh.slashcommands.internal.handlers.ModalInteractionHandler;
import net.aerh.slashcommands.internal.handlers.SlashCommandHandler;
import net.aerh.slashcommands.internal.registration.DiscordCommandRegistrationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main manager class for Discord slash commands using JDA.
 * Uses object-oriented design principles with separated concerns and dependency injection.
 *
 * <p>This class serves as the central coordinator for the slash command framework, delegating
 * specific responsibilities to specialized handler classes:
 * <ul>
 *   <li>Command registration via {@link DiscordCommandRegistrationService}</li>
 *   <li>Slash command handling via {@link SlashCommandHandler}</li>
 *   <li>Autocomplete handling via {@link AutocompleteHandler}</li>
 *   <li>Component interaction handling via {@link ComponentInteractionHandler}</li>
 *   <li>Modal interaction handling via {@link ModalInteractionHandler}</li>
 * </ul>
 *
 * <p>Usage example with builder pattern:
 * <pre>{@code
 * SlashCommandManager manager = SlashCommandManager.builder()
 *     .withJDA(jda)
 *     .addPermissionHandler(new CustomPermissionHandler())
 *     .registerCommands(new MyCommands(), new AdminCommands())
 *     .scanPackage("com.example.commands")
 *     .build();
 * }</pre>
 */
public class SlashCommandManager extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandManager.class);

    private final SlashCommandRegistry registry;
    private final PermissionManager permissionManager;
    private final DiscordCommandRegistrationService registrationService;
    private final SlashCommandHandler slashCommandHandler;
    private final AutocompleteHandler autocompleteHandler;
    private final ComponentInteractionHandler componentHandler;
    private final ModalInteractionHandler modalHandler;

    private JDA jda;

    /**
     * Constructs a new SlashCommandManager with the provided PermissionManager.
     * If no PermissionManager is provided, a default instance will be created.
     *
     * @param permissionManager the PermissionManager to use, or null to create a default one
     */
    SlashCommandManager(PermissionManager permissionManager) {
        this.registry = new SlashCommandRegistry();
        this.permissionManager = permissionManager != null ? permissionManager : new PermissionManager();

        // Initialise services and handlers
        this.registrationService = new DiscordCommandRegistrationService();
        this.slashCommandHandler = new SlashCommandHandler(registry, this.permissionManager);
        this.autocompleteHandler = new AutocompleteHandler(registry);
        this.componentHandler = new ComponentInteractionHandler(registry);
        this.modalHandler = new ModalInteractionHandler(registry);
    }

    /**
     * Creates a new builder for configuring the SlashCommandManager.
     *
     * @return a new SlashCommandManagerBuilder
     */
    public static SlashCommandManagerBuilder builder() {
        return SlashCommandManagerBuilder.builder();
    }

    /**
     * Sets the JDA instance and registers this manager as an event listener.
     * If JDA is already connected, commands will be registered immediately.
     *
     * @param jda the JDA instance to use
     * @return this SlashCommandManager for method chaining
     */
    public SlashCommandManager setJDA(JDA jda) {
        this.jda = jda;
        jda.addEventListener(this);

        if (jda.getStatus() == JDA.Status.CONNECTED) {
            registerCommandsWithJDA();
        }

        return this;
    }


    /**
     * Registers a custom permission handler.
     *
     * @param handler the permission handler to register
     * @return this SlashCommandManager for method chaining
     */
    public SlashCommandManager registerPermissionHandler(PermissionHandler handler) {
        permissionManager.registerHandler(handler);
        return this;
    }

    /**
     * Gets the permission manager for advanced permission configuration.
     *
     * @return the PermissionManager instance
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * Gets the command registry for advanced command inspection.
     *
     * @return the SlashCommandRegistry instance
     */
    public SlashCommandRegistry getRegistry() {
        return registry;
    }

    /**
     * Package-private method for builder to register commands.
     */
    void registerCommandsFromBuilder(Object... instances) {
        registry.registerCommands(instances);
    }

    /**
     * Package-private method for builder to scan packages.
     */
    void scanPackageFromBuilder(String packageName) {
        registry.scanAndRegister(packageName);
    }

    /**
     * Manually registers all commands with Discord.
     * This method is called automatically when JDA becomes ready.
     */
    public void registerCommandsWithJDA() {
        if (jda == null) {
            logger.warn("Cannot register commands: JDA instance is null");
            return;
        }

        registrationService.registerAllCommands(jda, registry);
    }

    @Override
    @SubscribeEvent
    public void onReady(@NotNull ReadyEvent event) {
        if (jda == null) {
            jda = event.getJDA();
            if (!jda.getEventManager().getRegisteredListeners().contains(this)) {
                jda.addEventListener(this);
            }
        }

        registerCommandsWithJDA();
    }

    @Override
    @SubscribeEvent
    public void onShutdown(@NotNull ShutdownEvent event) {
        permissionManager.clearHandlers();
    }

    @Override
    @SubscribeEvent
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        slashCommandHandler.handleSlashCommand(event);
    }

    @Override
    @SubscribeEvent
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        autocompleteHandler.handleAutocomplete(event);
    }

    @Override
    @SubscribeEvent
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        componentHandler.handleButtonInteraction(event);
    }

    @Override
    @SubscribeEvent
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        componentHandler.handleStringSelectInteraction(event);
    }

    @Override
    @SubscribeEvent
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        modalHandler.handleModalInteraction(event);
    }
}