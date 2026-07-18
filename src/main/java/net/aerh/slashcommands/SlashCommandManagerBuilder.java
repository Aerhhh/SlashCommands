package net.aerh.slashcommands;

import net.aerh.slashcommands.api.permissions.PermissionHandler;
import net.aerh.slashcommands.internal.core.PermissionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.hypixel.nerdbot.marmalade.pattern.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Builder for creating and configuring SlashCommandManager instances.
 * Provides a fluent interface for setting up the command manager with various options.
 *
 * <p>All command registration must be done through the builder before calling build().
 * Commands are registered with the framework during the build process.
 *
 * <p>Usage example:
 * <pre>{@code
 * SlashCommandManager manager = SlashCommandManager.builder()
 *     .withJDA(jda)
 *     .addPermissionHandler(new CustomPermissionHandler())
 *     .registerCommands(new MyCommands(), new AdminCommands())
 *     .scanPackage("com.example.commands")
 *     .build();
 * }</pre>
 */
public class SlashCommandManagerBuilder extends Builder<SlashCommandManager> {
    private final List<Object> commandInstances = new ArrayList<>();
    private final List<String> packagesToScan = new ArrayList<>();
    private JDA jda;
    private JDABuilder jdaBuilder;
    private PermissionManager permissionManager;
    private ExecutorService commandExecutor;

    private SlashCommandManagerBuilder() {
        this.permissionManager = new PermissionManager();
    }

    /**
     * Creates a new builder instance.
     *
     * @return a new SlashCommandManagerBuilder
     */
    public static SlashCommandManagerBuilder builder() {
        return new SlashCommandManagerBuilder();
    }

    /**
     * Sets the JDA instance to use.
     *
     * @param jda the JDA instance
     * @return this builder for method chaining
     * @throws IllegalArgumentException if jda is null
     */
    public SlashCommandManagerBuilder withJDA(JDA jda) {
        if (jda == null) {
            throw new IllegalArgumentException("JDA instance cannot be null");
        }
        this.jda = jda;
        this.jdaBuilder = null;
        return this;
    }

    /**
     * Sets the JDABuilder to register with.
     *
     * @param builder the JDABuilder instance
     * @return this builder for method chaining
     * @throws IllegalArgumentException if builder is null
     */
    public SlashCommandManagerBuilder withJDABuilder(JDABuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("JDABuilder instance cannot be null");
        }
        this.jdaBuilder = builder;
        this.jda = null;
        return this;
    }

    /**
     * Sets a custom permission manager.
     *
     * @param permissionManager the permission manager to use
     * @return this builder for method chaining
     */
    public SlashCommandManagerBuilder withPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
        return this;
    }

    /**
     * Sets the executor used to run command, button, string-select and modal handlers off the JDA
     * event thread. If not set, a default daemon-threaded cached pool is used and shut down when JDA
     * shuts down; a supplied executor is left for the caller to manage.
     *
     * @param commandExecutor the executor to dispatch handlers on
     * @return this builder for method chaining
     */
    public SlashCommandManagerBuilder withCommandExecutor(ExecutorService commandExecutor) {
        this.commandExecutor = commandExecutor;
        return this;
    }

    /**
     * Adds a permission handler to the permission manager.
     *
     * @param handler the permission handler to add
     * @return this builder for method chaining
     */
    public SlashCommandManagerBuilder addPermissionHandler(PermissionHandler handler) {
        this.permissionManager.registerHandler(handler);
        return this;
    }

    /**
     * Registers command handler instances to be registered with the manager.
     *
     * @param instances the command handler instances to register
     * @return this builder for method chaining
     * @throws IllegalArgumentException if any instance is null
     */
    public SlashCommandManagerBuilder registerCommands(Object... instances) {
        if (instances == null) {
            throw new IllegalArgumentException("Command instances array cannot be null");
        }

        for (Object instance : instances) {
            if (instance == null) {
                throw new IllegalArgumentException("Command instance cannot be null");
            }
        }

        this.commandInstances.addAll(Arrays.asList(instances));
        return this;
    }

    /**
     * Adds a package to scan for annotated command methods.
     *
     * @param packageName the package name to scan
     * @return this builder for method chaining
     * @throws IllegalArgumentException if packageName is null or empty
     */
    public SlashCommandManagerBuilder scanPackage(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }

        this.packagesToScan.add(packageName);
        return this;
    }

    /**
     * Validates the builder configuration before construction.
     * Checks that a JDA source, at least one command source, and a permission manager are all present.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    @Override
    protected void validate() {
        if (jda == null && jdaBuilder == null) {
            throw new IllegalStateException("Either JDA instance or JDABuilder must be provided using withJDA() or withJDABuilder()");
        }

        if (commandInstances.isEmpty() && packagesToScan.isEmpty()) {
            throw new IllegalStateException("No commands configured. Use registerCommands() or scanPackage() to add commands");
        }

        for (String packageName : packagesToScan) {
            if (packageName == null || packageName.trim().isEmpty()) {
                throw new IllegalStateException("Package name cannot be null or empty");
            }
        }

        for (Object instance : commandInstances) {
            if (instance == null) {
                throw new IllegalStateException("Command instance cannot be null");
            }
        }

        if (permissionManager == null) {
            throw new IllegalStateException("Permission manager cannot be null");
        }
    }

    /**
     * Constructs the SlashCommandManager from the validated builder state.
     *
     * @return the configured SlashCommandManager instance
     */
    @Override
    protected SlashCommandManager construct() {
        SlashCommandManager manager = new SlashCommandManager(permissionManager, commandExecutor);

        // Register configured commands and packages
        if (!commandInstances.isEmpty()) {
            manager.registerCommandsFromBuilder(commandInstances.toArray());
        }

        for (String packageName : packagesToScan) {
            manager.scanPackageFromBuilder(packageName);
        }

        // Set up JDA
        if (jda != null) {
            manager.setJDA(jda);
        } else if (jdaBuilder != null) {
            jdaBuilder.addEventListeners(manager);
        }

        return manager;
    }
}
