package tech.purelove.twswhitelist.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.twswhitelist.config.WhitelistConfig;
import tech.purelove.twswhitelist.discord.listener.WhitelistListener;
import tech.purelove.twswhitelist.discord.listener.WhitelistSlashCommand;

import java.util.Objects;

public class DiscordBot {

    private final WhitelistConfig config;
    private final JavaPlugin plugin;
    private JDA jda;

    public DiscordBot(JavaPlugin plugin, WhitelistConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start() throws Exception {
        try {
            Message.suppressContentIntentWarning();

            jda = JDABuilder.createDefault(config.bot().token())
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .addEventListeners(
                            new WhitelistListener(config),
                            new WhitelistSlashCommand(plugin, config)
                    )
                    .build();

            jda.awaitReady();

            registerCommands();

        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }

    private void registerCommands() {
        var guild = jda.getGuildById(config.bot().serverId());

        if (guild == null) {
            throw new IllegalStateException("Bot cannot find Discord server/guild: " + config.bot().serverId());
        }

        guild.updateCommands()
                .addCommands(
                        Commands.slash("devlist", "Manage the Minecraft whitelist")
                                .addSubcommands(
                                        new SubcommandData("add", "Add a player to the whitelist")
                                                .addOption(OptionType.STRING, "username", "Minecraft username", true),
                                        new SubcommandData("remove", "Remove a player from the whitelist")
                                                .addOption(OptionType.STRING, "username", "Minecraft username", true)
                                )
                )
                .queue();
    }

    public void shutdown() {
        if (jda == null) return;

        try {
            jda.shutdownNow();

            if (!jda.awaitShutdown(10, java.util.concurrent.TimeUnit.SECONDS)) {
                System.out.println("JDA did not shut down cleanly within 10 seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            jda = null;
        }
    }
}

