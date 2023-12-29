package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.auth.*;
import org.bukkit.ChatColor;

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

    public static String stripAllColor(String input)
    {
        return ChatColor.stripColor(stripUnprocessedColor(input));
    }

    public static AuthPluginHandler selectAuthPlugin()
    {
        for (AuthPluginHandler handler : SUPPORTED_AUTH_HANDLERS)
            if (handler.isInstalled()) return handler;
        throw new RuntimeException("No supported auth plugins installed");
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
