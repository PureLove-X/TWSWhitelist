package tech.purelove.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogUtils {

    private static Logger log;
    private static boolean initialized = false;

    private LogUtils() {}

    public static void init() {
        if (initialized) return;

        log = Logger.getLogger("TWS Whitelist");
        log.setUseParentHandlers(false);
        log.setLevel(Level.ALL);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleColoredFormatter("[TWS Whitelist]"));

        log.addHandler(handler);
        initialized = true;
    }

    private static void send(String message) {
        log.log(Level.INFO, message);
    }

    // Color helpers
    public static void white(String msg) {
        send(SimpleColoredFormatter.WHITE + msg);
    }

    public static void green(String msg) {
        send(SimpleColoredFormatter.GREEN + msg);
    }

    public static void yellow(String msg) {
        send(SimpleColoredFormatter.YELLOW + msg);
    }

    public static void red(String msg) {
        send(SimpleColoredFormatter.RED + msg);
    }

    public static void aqua(String msg) {
        send(SimpleColoredFormatter.AQUA + msg);
    }

    public static void gray(String msg) {
        send(SimpleColoredFormatter.GRAY + msg);
    }
}
