package tech.purelove.twswhitelist.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogUtils {

    private static Logger log;
    private static boolean initialized = false;

    private LogUtils() {}

    public static void init(JavaPlugin plugin) {
        if (initialized) return;
        log = plugin.getLogger();
        initialized = true;
    }

    private static void send(Level level, String message) {
        log.log(level, message + tech.purelove.twswhitelist.util.SimpleColoredFormatter.RESET);
    }

    // Color helpers
    public static void info(String msg) {
        send(Level.INFO, tech.purelove.twswhitelist.util.SimpleColoredFormatter.WHITE + msg + tech.purelove.twswhitelist.util.SimpleColoredFormatter.RESET);
    }

    public static void success(String msg) {
        send(Level.INFO, tech.purelove.twswhitelist.util.SimpleColoredFormatter.GREEN + msg + tech.purelove.twswhitelist.util.SimpleColoredFormatter.RESET);
    }

    public static void warn(String msg) {
        send(Level.WARNING, tech.purelove.twswhitelist.util.SimpleColoredFormatter.YELLOW + msg + tech.purelove.twswhitelist.util.SimpleColoredFormatter.RESET);
    }

    public static void error(String msg) {
        send(Level.SEVERE, tech.purelove.twswhitelist.util.SimpleColoredFormatter.RED + msg + tech.purelove.twswhitelist.util.SimpleColoredFormatter.RESET);
    }

    public static void debug(String msg) {
        send(Level.FINE, tech.purelove.twswhitelist.util.SimpleColoredFormatter.GRAY + msg + tech.purelove.twswhitelist.util.SimpleColoredFormatter.RESET);
    }

}
