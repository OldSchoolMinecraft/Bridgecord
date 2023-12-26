package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.auth.AuthMeHandler;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.auth.OSASHandler;
import net.oldschoolminecraft.bcord.auth.xAuthHandler;

import java.util.*;

public class Util
{
    private static Random rng = new Random();

    private static final List<AuthPluginHandler> SUPPORTED_AUTH_HANDLERS = new ArrayList<>();

    static
    {
        try
        {
            SUPPORTED_AUTH_HANDLERS.add(new OSASHandler());
        } catch (NoClassDefFoundError ignored) {} // not installed

        try
        {
            SUPPORTED_AUTH_HANDLERS.add(new AuthMeHandler());
        } catch (NoClassDefFoundError ignored) {} // not installed

        try
        {
            SUPPORTED_AUTH_HANDLERS.add(new xAuthHandler());
        } catch (NoClassDefFoundError ignored) {} // not installed
    }

    public static String stripUnprocessedColor(String input)
    {
        return input == null ? null : input.replaceAll("(?i)&[0-F]", "");
    }

    public static AuthPluginHandler selectAuthPlugin()
    {
        return SUPPORTED_AUTH_HANDLERS.stream()
                .filter(AuthPluginHandler::isInstalled)
                .reduce((first, second) -> {
                    throw new RuntimeException("Multiple auth plugins are installed. Please remove one of them.");
                })
                .orElseThrow(() -> new RuntimeException("No auth plugin is installed."));
    }

    public static List<String> splitString(String input, int nbrOfChars)
    {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < input.length(); i += nbrOfChars)
        {
            String part = input.substring(i, Math.min(input.length(), i + nbrOfChars));
            chunks.add(part);
        }
        return chunks;
    }

    public static String generateString(String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) text[i] = characters.charAt(rng.nextInt(characters.length()));
        return new String(text);
    }

    public static String generateSecurePassword()
    {
        return generateString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 12);
    }

    public static String processMessage(String message, HashMap<String, String> data)
    {
        String pre = message;
        for (String key : data.keySet())
            pre = pre.replace(key, data.get(key));
        return pre;
    }
}
