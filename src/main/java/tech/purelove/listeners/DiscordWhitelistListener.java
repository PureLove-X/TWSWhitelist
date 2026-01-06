package tech.purelove.listeners;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import org.bukkit.Bukkit;
import tech.purelove.TWSWhitelistPlugin;
import tech.purelove.util.LogUtils;

public class DiscordWhitelistListener {

    private final TWSWhitelistPlugin plugin;

    public DiscordWhitelistListener(TWSWhitelistPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onMessage(DiscordGuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(plugin.getWhitelistChannelId())) return;

        String raw = event.getMessage().getContentRaw().trim();
        if (!raw.toLowerCase().startsWith(plugin.getCommandPrefix().toLowerCase())) return;

        String[] args = raw.split("\\s+");
        if (args.length < 3) return;

        String action = args[1].toLowerCase();
        String username = args[2];

        switch (action) {
            case "add" ->
                    runWhitelistCommand(
                            event,
                            "whitelist add " + username,
                            "whitelisted",
                            username
                    );

            case "remove" ->
                    runWhitelistCommand(
                            event,
                            "whitelist remove " + username,
                            "removed from whitelist",
                            username
                    );
        }
    }
    private void runWhitelistCommand(
            DiscordGuildMessageReceivedEvent event,
            String command,
            String actionText,
            String username
    ) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            LogUtils.green(
                    "Whitelist " + actionText
                            + " by " + event.getAuthor().getName()
                            + " → " + username
            );

            // Discord reply
            event.getChannel()
                    .sendMessage("✅ **" + username + "** has been " + actionText + ".")
                    .queue();
        });
    }
}
