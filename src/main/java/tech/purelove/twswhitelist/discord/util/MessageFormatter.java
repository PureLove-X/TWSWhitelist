package tech.purelove.twswhitelist.discord.util;

import java.util.Map;

public final class MessageFormatter {

    private MessageFormatter() {}

    public static String format(String template, Map<String, String> values) {
        if (template == null || template.isBlank()) {
            return " "; // JDA-safe: prevents empty message crash
        }

        String result = resolveChannelMentions(template);

        for (var entry : values.entrySet()) {
            result = result.replace(
                    "{" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : entry.getValue()
            );
        }

        return result;
    }

    private static String resolveChannelMentions(String input) {
        return input.replaceAll("\\{channel:(\\d+)}", "<#$1>");
    }
}
