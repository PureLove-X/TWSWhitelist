package tech.purelove.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleColoredFormatter extends Formatter {

    public static final String RESET  = "\u001B[0m";
    public static final String RED    = "\u001B[31m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String AQUA   = "\u001B[36m";
    public static final String WHITE  = "\u001B[37m";
    public static final String GRAY   = "\u001B[90m";

    private final String prefix;

    public SimpleColoredFormatter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String format(LogRecord record) {
        return prefix + " " +
                record.getMessage() +
                RESET + "\n";
    }
}
