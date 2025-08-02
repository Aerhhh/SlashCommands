package net.aerh.slashcommands.internal.core;

import net.aerh.slashcommands.api.annotations.SlashAutocompleteHandler;
import net.aerh.slashcommands.api.annotations.SlashCommand;
import net.aerh.slashcommands.api.annotations.SlashComponentHandler;
import net.aerh.slashcommands.api.annotations.SlashModalHandler;
import net.aerh.slashcommands.internal.validation.CommandValidator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Registry for managing slash commands, autocomplete handlers, component interactions, and modal dialogues.
 * This class handles the discovery, registration, and retrieval of annotated methods for Discord interactions.
 *
 * <p>The registry supports:
 * <ul>
 *   <li>Regular slash commands, with subcommands and groups</li>
 *   <li>Autocomplete handlers</li>
 *   <li>Component interactions (buttons, select menus)</li>
 *   <li>Modal dialogue handlers</li>
 * </ul>
 */
public class SlashCommandRegistry {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandRegistry.class);
    private final Map<String, CommandInfo> commands = new HashMap<>();
    private final Map<String, Map<String, CommandInfo>> subcommandGroups = new HashMap<>();
    private final Map<String, Map<String, Map<String, CommandInfo>>> subcommands = new HashMap<>();
    private final Map<String, AutocompleteInfo> autocompleteHandlers = new HashMap<>();
    private final Map<String, ComponentInfo> componentHandlers = new HashMap<>();
    private final Map<String, ModalInfo> modalHandlers = new HashMap<>();

    /**
     * Registers command handlers from the provided instances.
     * Scans each instance for methods annotated with {@link SlashCommand}, {@link SlashAutocompleteHandler},
     * {@link SlashComponentHandler}, and {@link SlashModalHandler} annotations.
     *
     * @param instances the command handler instances to register
     */
    public void registerCommands(Object... instances) {
        for (Object instance : instances) {
            registerInstance(instance);
        }
    }

    /**
     * Scans the specified package for annotated methods and automatically registers them.
     * Creates instances of classes containing annotated methods using their default constructors.
     *
     * @param packageName the package name to scan (e.g. "com.example.commands")
     * @throws RuntimeException if unable to create instances of discovered classes
     */
    public void scanAndRegister(String packageName) {
        logger.info("Scanning package '{}' for annotated methods", packageName);
        Reflections reflections = new Reflections(packageName, Scanners.MethodsAnnotated);

        Set<Method> commandMethods = reflections.getMethodsAnnotatedWith(SlashCommand.class);
        Set<Method> autocompleteMethods = reflections.getMethodsAnnotatedWith(SlashAutocompleteHandler.class);
        Set<Method> componentMethods = reflections.getMethodsAnnotatedWith(SlashComponentHandler.class);
        Set<Method> modalMethods = reflections.getMethodsAnnotatedWith(SlashModalHandler.class);

        logger.info("Found {} command methods, {} autocomplete methods, {} component methods, {} modal methods",
                commandMethods.size(), autocompleteMethods.size(), componentMethods.size(), modalMethods.size());

        // Validate handler ID uniqueness before registration
        validateHandlerIdUniqueness(autocompleteMethods, componentMethods, modalMethods);

        Map<Class<?>, Object> instanceCache = new HashMap<>();

        for (Method method : commandMethods) {
            Object instance = getOrCreateInstance(method.getDeclaringClass(), instanceCache);
            registerCommandMethod(instance, method);
        }

        for (Method method : autocompleteMethods) {
            Object instance = getOrCreateInstance(method.getDeclaringClass(), instanceCache);
            registerAutocompleteMethod(instance, method);
        }

        for (Method method : componentMethods) {
            Object instance = getOrCreateInstance(method.getDeclaringClass(), instanceCache);
            registerComponentMethod(instance, method);
        }

        for (Method method : modalMethods) {
            Object instance = getOrCreateInstance(method.getDeclaringClass(), instanceCache);
            registerModalMethod(instance, method);
        }

        logger.debug("Package scan completed. Created {} class instances", instanceCache.size());
    }

    private Object getOrCreateInstance(Class<?> clazz, Map<Class<?>, Object> instanceCache) {
        return instanceCache.computeIfAbsent(clazz, k -> {
            try {
                return k.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error("Failed to create instance of class: {} - {}", k.getName(), e.getMessage());
                throw new RuntimeException("Failed to create instance of " + k.getName(), e);
            }
        });
    }

    private void registerInstance(Object instance) {
        Class<?> clazz = instance.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        int commandCount = 0, autocompleteCount = 0, componentCount = 0, modalCount = 0;

        for (Method method : methods) {
            if (method.isAnnotationPresent(SlashCommand.class)) {
                registerCommandMethod(instance, method);
                commandCount++;
            }

            if (method.isAnnotationPresent(SlashAutocompleteHandler.class)) {
                registerAutocompleteMethod(instance, method);
                autocompleteCount++;
            }

            if (method.isAnnotationPresent(SlashComponentHandler.class)) {
                registerComponentMethod(instance, method);
                componentCount++;
            }

            if (method.isAnnotationPresent(SlashModalHandler.class)) {
                registerModalMethod(instance, method);
                modalCount++;
            }
        }

        logger.info("Registered from {}: {} commands, {} autocomplete handlers, {} component handlers, {} modal handlers",
                clazz.getSimpleName(), commandCount, autocompleteCount, componentCount, modalCount);
    }

    private void registerCommandMethod(Object instance, Method method) {
        SlashCommand annotation = method.getAnnotation(SlashCommand.class);

        var validationErrors = CommandValidator.validateCommand(method);
        if (!validationErrors.isEmpty()) {
            logger.error("Validation errors for command '{}' in method {}. Skipping registration:", annotation.name(), method.getName());
            validationErrors.forEach(error -> logger.error("  {}", error.message()));
            return; // Skip this command but continue with others
        }

        CommandInfo commandInfo = new CommandInfo(instance, method, annotation);

        if (!annotation.subcommand().isEmpty()) {
            if (!annotation.group().isEmpty()) {
                subcommands.computeIfAbsent(annotation.name(), k -> new HashMap<>())
                        .computeIfAbsent(annotation.group(), k -> new HashMap<>())
                        .put(annotation.subcommand(), commandInfo);
            } else {
                subcommandGroups.computeIfAbsent(annotation.name(), k -> new HashMap<>())
                        .put(annotation.subcommand(), commandInfo);
            }
        } else {
            commands.put(annotation.name(), commandInfo);
        }
    }

    private String getCommandPath(SlashCommand annotation) {
        StringBuilder path = new StringBuilder("/" + annotation.name());
        if (!annotation.group().isEmpty()) {
            path.append(" ").append(annotation.group());
        }
        if (!annotation.subcommand().isEmpty()) {
            path.append(" ").append(annotation.subcommand());
        }
        return path.toString();
    }

    private void registerAutocompleteMethod(Object instance, Method method) {
        SlashAutocompleteHandler annotation = method.getAnnotation(SlashAutocompleteHandler.class);
        autocompleteHandlers.put(annotation.id(), new AutocompleteInfo(instance, method, annotation));
    }

    private void registerComponentMethod(Object instance, Method method) {
        SlashComponentHandler annotation = method.getAnnotation(SlashComponentHandler.class);
        componentHandlers.put(annotation.id(), new ComponentInfo(instance, method, annotation));

        for (String pattern : annotation.patterns()) {
            componentHandlers.put(pattern, new ComponentInfo(instance, method, annotation));
        }
    }

    private void registerModalMethod(Object instance, Method method) {
        SlashModalHandler annotation = method.getAnnotation(SlashModalHandler.class);

        if (annotation.patterns().length > 0) {
            // Use patterns if specified
            for (String pattern : annotation.patterns()) {
                modalHandlers.put(pattern, new ModalInfo(instance, method, annotation));
            }
        } else {
            // Use single ID
            modalHandlers.put(annotation.id(), new ModalInfo(instance, method, annotation));
        }
    }

    /**
     * Retrieves a registered command by name.
     *
     * @param name the command name
     * @return the {@link CommandInfo} for the command, or null if not found
     */
    public CommandInfo getCommand(String name) {
        return commands.get(name);
    }

    /**
     * Retrieves a subcommand (not in a group).
     *
     * @param commandName    the parent command name
     * @param subcommandName the subcommand name
     * @return the {@link CommandInfo} for the subcommand, or null if not found
     */
    public CommandInfo getSubcommand(String commandName, String subcommandName) {
        Map<String, CommandInfo> subcommandMap = subcommandGroups.get(commandName);
        return subcommandMap != null ? subcommandMap.get(subcommandName) : null;
    }

    /**
     * Retrieves a subcommand from a subcommand group.
     *
     * @param commandName    the parent command name
     * @param groupName      the subcommand group name
     * @param subcommandName the subcommand name
     * @return the {@link CommandInfo} for the subcommand, or null if not found
     */
    public CommandInfo getSubcommand(String commandName, String groupName, String subcommandName) {
        Map<String, Map<String, CommandInfo>> commandMap = subcommands.get(commandName);
        if (commandMap != null) {
            Map<String, CommandInfo> groupMap = commandMap.get(groupName);
            return groupMap != null ? groupMap.get(subcommandName) : null;
        }
        return null;
    }

    /**
     * Retrieves an autocomplete handler by its unique ID.
     *
     * @param autocompleteId the autocomplete handler ID
     * @return the {@link AutocompleteInfo} for the handler, or null if not found
     */
    public AutocompleteInfo getAutocompleteHandler(String autocompleteId) {
        return autocompleteHandlers.get(autocompleteId);
    }

    /**
     * Finds the autocomplete ID for a specific command and option.
     *
     * @param commandName the command name
     * @param optionName  the option name
     * @return the autocomplete ID, or null if not found or no autocomplete configured
     */
    public String findAutocompleteId(String commandName, String optionName) {
        CommandInfo commandInfo = getCommand(commandName);
        if (commandInfo != null) {
            return findAutocompleteIdInCommand(commandInfo, optionName);
        }

        // Try find via subcommands
        Map<String, CommandInfo> subcommandMap = subcommandGroups.get(commandName);
        if (subcommandMap != null) {
            for (CommandInfo subcommandInfo : subcommandMap.values()) {
                String autocompleteId = findAutocompleteIdInCommand(subcommandInfo, optionName);
                if (autocompleteId != null) {
                    return autocompleteId;
                }
            }
        }

        // Try groups
        Map<String, Map<String, CommandInfo>> commandGroups = subcommands.get(commandName);
        if (commandGroups != null) {
            for (Map<String, CommandInfo> group : commandGroups.values()) {
                for (CommandInfo subcommandInfo : group.values()) {
                    String autocompleteId = findAutocompleteIdInCommand(subcommandInfo, optionName);
                    if (autocompleteId != null) {
                        return autocompleteId;
                    }
                }
            }
        }

        return null;
    }

    private String findAutocompleteIdInCommand(CommandInfo commandInfo, String optionName) {
        for (CommandInfo.OptionInfo optionInfo : commandInfo.getOptions()) {
            if (optionName.equals(optionInfo.resolvedName()) &&
                    !optionInfo.annotation().autocompleteId().isEmpty()) {
                return optionInfo.annotation().autocompleteId();
            }
        }

        return null;
    }

    /**
     * Retrieves a component handler for the given component ID.
     * Supports both exact matches and wildcard pattern matching (e.g., "button_*").
     *
     * @param componentId the component ID from the interaction
     * @return the {@link ComponentInfo} for the handler, or null if not found
     */
    public ComponentInfo getComponentHandler(String componentId) {
        ComponentInfo handler = componentHandlers.get(componentId);
        if (handler != null) {
            return handler;
        }

        for (Map.Entry<String, ComponentInfo> entry : componentHandlers.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.contains("*") && matchesPattern(componentId, pattern)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Retrieves a modal handler for the given modal ID.
     * Supports both exact matches and wildcard pattern matching (e.g., "report_*").
     *
     * @param modalId the modal ID from the interaction
     * @return the {@link ModalInfo} for the handler, or null if not found
     */
    public ModalInfo getModalHandler(String modalId) {
        ModalInfo handler = modalHandlers.get(modalId);
        if (handler != null) {
            return handler;
        }

        for (Map.Entry<String, ModalInfo> entry : modalHandlers.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.contains("*") && matchesPattern(modalId, pattern)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private boolean matchesPattern(String componentId, String pattern) {
        String regex = pattern.replace("*", ".*");
        return componentId.matches(regex);
    }

    /**
     * Validates that all handler IDs are unique across autocomplete, component, and modal handlers.
     *
     * @param autocompleteMethods methods annotated with @SlashAutocompleteHandler
     * @param componentMethods    methods annotated with @SlashComponentHandler
     * @param modalMethods        methods annotated with @SlashModalHandler
     * @throws IllegalArgumentException if duplicate IDs are found
     */
    private void validateHandlerIdUniqueness(Set<Method> autocompleteMethods, Set<Method> componentMethods, Set<Method> modalMethods) {
        Map<String, List<String>> idToHandlers = new HashMap<>(); // ID -> list of handler descriptions

        // Collect all handler IDs and their locations
        for (Method method : autocompleteMethods) {
            SlashAutocompleteHandler annotation = method.getAnnotation(SlashAutocompleteHandler.class);
            String id = annotation.id();
            idToHandlers.computeIfAbsent(id, k -> new ArrayList<>())
                    .add("AutocompleteHandler(" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + ")");
        }

        for (Method method : componentMethods) {
            SlashComponentHandler annotation = method.getAnnotation(SlashComponentHandler.class);
            String id = annotation.id();
            idToHandlers.computeIfAbsent(id, k -> new ArrayList<>())
                    .add("ComponentHandler(" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + ")");

            for (String pattern : annotation.patterns()) {
                idToHandlers.computeIfAbsent(pattern, k -> new ArrayList<>())
                        .add("ComponentHandler pattern(" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + ")");
            }
        }

        for (Method method : modalMethods) {
            SlashModalHandler annotation = method.getAnnotation(SlashModalHandler.class);
            String id = annotation.id();
            idToHandlers.computeIfAbsent(id, k -> new ArrayList<>())
                    .add("ModalHandler(" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + ")");

            for (String pattern : annotation.patterns()) {
                idToHandlers.computeIfAbsent(pattern, k -> new ArrayList<>())
                        .add("ModalHandler pattern(" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + ")");
            }
        }

        // Check for duplicates
        List<String> duplicates = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : idToHandlers.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.add("'" + entry.getKey() + "' used in " + String.join(", ", entry.getValue()));
            }
        }

        if (!duplicates.isEmpty()) {
            String errorMessage = "Duplicate handler IDs: " + String.join("; ", duplicates);
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        logger.debug("Handler ID validation passed: {} unique IDs registered", idToHandlers.size());
    }

    /**
     * Returns a copy of all registered top-level commands.
     *
     * @return a map of command names to CommandInfo objects
     */
    public Map<String, CommandInfo> getAllCommands() {
        return new HashMap<>(commands);
    }

    /**
     * Returns a copy of all registered subcommand groups.
     *
     * @return a nested map of command names to subcommand names to CommandInfo objects
     */
    public Map<String, Map<String, CommandInfo>> getAllSubcommandGroups() {
        return new HashMap<>(subcommandGroups);
    }

    /**
     * Returns a copy of all registered subcommands.
     *
     * @return a triple-nested map of command names to group names to subcommand names to {@link CommandInfo} objects
     */
    public Map<String, Map<String, Map<String, CommandInfo>>> getAllSubcommands() {
        return new HashMap<>(subcommands);
    }

    /**
     * Information about a registered autocomplete handler.
     */
    public record AutocompleteInfo(Object instance, Method method, SlashAutocompleteHandler annotation) {
    }

    /**
     * Information about a registered component interaction handler.
     */
    public record ComponentInfo(Object instance, Method method, SlashComponentHandler annotation) {
    }

    /**
     * Information about a registered modal dialogue handler.
     */
    public record ModalInfo(Object instance, Method method, SlashModalHandler annotation) {
    }
}