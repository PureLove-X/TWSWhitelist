package tech.purelove.twswhitelist;

import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.twswhitelist.config.ConfigLoader;
import tech.purelove.twswhitelist.config.WhitelistConfig;
import tech.purelove.twswhitelist.discord.DiscordBot;
import tech.purelove.twswhitelist.util.LogUtils;

public class TWSWhitelistPlugin extends JavaPlugin {

    private WhitelistConfig whitelistConfig;
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        LogUtils.init(this);
        saveDefaultConfig();

        whitelistConfig = ConfigLoader.load(this);

        if (whitelistConfig.bot().token() == null || whitelistConfig.bot().token().isBlank()) {
            LogUtils.error("Discord bot token is missing");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        discordBot = new DiscordBot(this, whitelistConfig);
        try {
            discordBot.start();
        } catch (Exception e) {
            LogUtils.error("Failed to start Discord bot", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }
    }
}
