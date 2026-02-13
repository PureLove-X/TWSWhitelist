package tech.purelove.twswhitelist.whitelist;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class WhitelistService {

    private WhitelistService() {}

    public static CompletableFuture<WhitelistResult> whitelist(String username) {
        return CompletableFuture.supplyAsync(() -> {

            // 1️⃣ format check
            if (!username.matches("^[A-Za-z0-9_]{3,16}$")) {
                return WhitelistResult.FAILED;
            }

            // 2️⃣ existence check (THIS fixes typos)
            if (!mojangUsernameExists(username)) {
                return WhitelistResult.FAILED;
            }

            // 3️⃣ main-thread whitelist
            CompletableFuture<WhitelistResult> result = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(
                    JavaPlugin.getProvidingPlugin(WhitelistService.class),
                    () -> {
                        try {
                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    "whitelist add " + username
                            );
                            result.complete(WhitelistResult.SUCCESS);
                        } catch (Exception e) {
                            result.complete(WhitelistResult.ERROR);
                        }
                    }
            );

            return result.join();
        });
    }

    private static boolean mojangUsernameExists(String username) {
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    URI.create("https://api.mojang.com/users/profiles/minecraft/" + username)
                            .toURL()
                            .openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
