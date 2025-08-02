package net.aerh.slashcommands.examples;

import net.aerh.slashcommands.SlashCommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example main class showing how to set up a Discord bot with the SlashCommands framework
 */
public class ExampleBot {
    private static final Logger logger = LoggerFactory.getLogger(ExampleBot.class);

    private static final String BOT_TOKEN = "YOUR_BOT_TOKEN_HERE";
    private static final String BOT_OWNER_ID = "YOUR_USER_ID_HERE";

    public static void main(String[] args) {
        try {
            JDA jda = setup();
            logger.info("Bot ready: {} in {} guilds", jda.getSelfUser().getAsTag(), jda.getGuilds().size());
        } catch (Exception e) {
            logger.error("Failed to start bot: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public static JDA setup() throws Exception {
        JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN)
                .setActivity(Activity.playing("with slash commands!"))
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false);

        SlashCommandManager.builder()
                .withJDABuilder(builder)
                .addPermissionHandler(new CustomPermissionHandler(BOT_OWNER_ID))
                .registerCommands(new ExampleCommands())
                .build();

        return builder.build().awaitReady();
    }
}