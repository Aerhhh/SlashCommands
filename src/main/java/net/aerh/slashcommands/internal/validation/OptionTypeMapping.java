package net.aerh.slashcommands.internal.validation;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralised mapping between Java parameter types and Discord {@link OptionType}.
 * Provides both type inference and compatibility checking functionality.
 */
public class OptionTypeMapping {

    private static final Map<OptionType, TypeConfig> TYPE_CONFIGS = new EnumMap<>(OptionType.class);

    static {
        TYPE_CONFIGS.put(OptionType.STRING, new TypeConfig(
                Set.of(String.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.INTEGER, new TypeConfig(
                Set.of(int.class, Integer.class, long.class, Long.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.BOOLEAN, new TypeConfig(
                Set.of(boolean.class, Boolean.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.NUMBER, new TypeConfig(
                Set.of(double.class, Double.class, float.class, Float.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.USER, new TypeConfig(
                Set.of(User.class, Member.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.ROLE, new TypeConfig(
                Set.of(Role.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.MENTIONABLE, new TypeConfig(
                Set.of(IMentionable.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.ATTACHMENT, new TypeConfig(
                Set.of(Message.Attachment.class),
                Set.of(),
                null
        ));

        TYPE_CONFIGS.put(OptionType.CHANNEL, new TypeConfig(
                Set.of(GuildChannel.class),
                Set.of(),
                "Channel types (TextChannel, VoiceChannel, etc.)"
        ));
    }

    /**
     * Infers the Discord {@link OptionType} from a Java parameter type.
     *
     * @param parameterType the Java class type of the parameter
     * @return the corresponding Discord {@link OptionType}, or STRING as fallback
     */
    public static OptionType inferOptionType(Class<?> parameterType) {
        for (Map.Entry<OptionType, TypeConfig> entry : TYPE_CONFIGS.entrySet()) {
            if (entry.getValue().exactTypes().contains(parameterType)) {
                return entry.getKey();
            }
        }

        String className = parameterType.getSimpleName();
        String fullClassName = parameterType.getName();

        for (Map.Entry<OptionType, TypeConfig> entry : TYPE_CONFIGS.entrySet()) {
            if (entry.getValue().classNames().contains(className)) {
                return entry.getKey();
            }
        }

        if (className.contains("Channel") || fullClassName.contains("channel")) {
            return OptionType.CHANNEL;
        }

        // Default fallback
        return OptionType.STRING;
    }

    /**
     * Checks if a Java parameter type is compatible with a Discord {@link OptionType}.
     *
     * @param parameterType the Java class type to check
     * @param optionType    the Discord {@link OptionType} to validate against
     * @return true if the types are compatible, false otherwise
     */
    public static boolean isCompatibleType(Class<?> parameterType, OptionType optionType) {
        TypeConfig config = TYPE_CONFIGS.get(optionType);
        if (config == null) {
            return false;
        }

        if (config.exactTypes().contains(parameterType)) {
            return true;
        }

        String className = parameterType.getSimpleName();
        String fullClassName = parameterType.getName();

        if (config.classNames().contains(className)) {
            return true;
        }

        return optionType == OptionType.CHANNEL &&
                (className.contains("Channel") || fullClassName.contains("channel"));
    }

    /**
     * Gets a human-readable description of compatible Java types for an {@link OptionType}.
     * Useful for error messages and documentation.
     *
     * @param optionType the Discord {@link OptionType}
     * @return a string describing compatible Java types
     */
    public static String getCompatibleTypesDescription(OptionType optionType) {
        TypeConfig config = TYPE_CONFIGS.get(optionType);
        if (config == null) {
            return "Unknown";
        }

        if (!config.exactTypes().isEmpty()) {
            return config.exactTypes().stream()
                    .map(Class::getSimpleName)
                    .sorted()
                    .collect(Collectors.joining(", "));
        }

        return config.description();
    }

    /**
     * Configuration for each OptionType, containing all related type information.
     */
    private record TypeConfig(Set<Class<?>> exactTypes, Set<String> classNames, String description) {
    }
}