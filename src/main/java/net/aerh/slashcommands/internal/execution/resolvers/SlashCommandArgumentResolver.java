package net.aerh.slashcommands.internal.execution.resolvers;

import net.aerh.slashcommands.internal.core.CommandInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Resolves arguments for slash command method invocation.
 * Handles mapping Discord option values to Java method parameters.
 */
public class SlashCommandArgumentResolver implements ArgumentResolver<SlashCommandInteractionEvent> {

    @Override
    public Object[] prepareArguments(Method method, SlashCommandInteractionEvent event) {
        // This method needs CommandInfo to resolve options properly
        throw new UnsupportedOperationException("Use prepareArguments(CommandInfo, SlashCommandInteractionEvent) instead");
    }

    /**
     * Prepares method arguments for slash command invocation.
     *
     * @param commandInfo the command information containing option mappings
     * @param event       the slash command interaction event
     * @return array of arguments ready for method invocation
     */
    public Object[] prepareArguments(CommandInfo commandInfo, SlashCommandInteractionEvent event) {
        Method method = commandInfo.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            if (param.getType() == SlashCommandInteractionEvent.class) {
                args[i] = event;
            } else {
                int finalI = i;
                CommandInfo.OptionInfo optionInfo = commandInfo.getOptions().stream()
                        .filter(opt -> opt.parameterIndex() == finalI)
                        .findFirst()
                        .orElse(null);

                if (optionInfo != null) {
                    args[i] = getOptionValue(event, optionInfo);
                }
            }
        }

        return args;
    }

    private Object getOptionValue(SlashCommandInteractionEvent event, CommandInfo.OptionInfo optionInfo) {
        String optionName = optionInfo.resolvedName();
        OptionType optionType = optionInfo.resolvedType();

        return switch (optionType) {
            case STRING -> event.getOption(optionName, "", OptionMapping::getAsString);
            case INTEGER -> event.getOption(optionName, 0, OptionMapping::getAsInt);
            case BOOLEAN -> event.getOption(optionName, false, OptionMapping::getAsBoolean);
            case USER -> {
                if (Member.class.isAssignableFrom(optionInfo.parameter().getType())) {
                    yield event.isFromGuild() ? event.getOption(optionName, OptionMapping::getAsMember) : null;
                } else {
                    yield event.getOption(optionName, OptionMapping::getAsUser);
                }
            }
            case CHANNEL -> event.getOption(optionName, OptionMapping::getAsChannel);
            case ROLE -> event.getOption(optionName, OptionMapping::getAsRole);
            case MENTIONABLE -> event.getOption(optionName, OptionMapping::getAsMentionable);
            case NUMBER -> event.getOption(optionName, 0.0, OptionMapping::getAsDouble);
            case ATTACHMENT -> event.getOption(optionName, OptionMapping::getAsAttachment);
            default -> null;
        };
    }
}