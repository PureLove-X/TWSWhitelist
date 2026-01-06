package tech.purelove;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.listeners.DiscordWhitelistListener;
import tech.purelove.util.LogUtils;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class TWSWhitelistPlugin extends JavaPlugin {

    private String whitelistChannelId;
    private String commandPrefix;

    @Override
    public void onEnable() {
        // config
        saveDefaultConfig();
        reloadLocalConfig();

        // logging
        LogUtils.init();
        LogUtils.aqua("Starting plugin");
        LogUtils.gray("Whitelist channel: " + whitelistChannelId);

        // DiscordSRV hook
        DiscordSRV.api.subscribe(new DiscordWhitelistListener(this));
        LogUtils.green("DiscordSRV listener registered");
    }

    public void reloadLocalConfig() {
        reloadConfig();
        this.whitelistChannelId = getConfig().getString("Discord.Channel_ID", "").trim();
        this.commandPrefix = getConfig().getString("Discord.Command_Prefix", "").trim();
    }

    public String getWhitelistChannelId() {
        return whitelistChannelId;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }
}