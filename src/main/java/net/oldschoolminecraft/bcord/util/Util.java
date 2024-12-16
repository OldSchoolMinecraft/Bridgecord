package net.oldschoolminecraft.bcord.util;

import com.google.gson.Gson;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.auth.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;

public class Util
{
    private static Random rng = new Random();
    private static Gson gson = new Gson();

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

    public static void saveVouch(String voucher, String vouched) throws IOException
    {
        File vouchDir = new File(Bridgecord.getInstance().getDataFolder(), "vouchers/" + voucher + "/");
        if (!vouchDir.getParentFile().exists()) vouchDir.getParentFile().mkdirs();
        if (!vouchDir.exists()) vouchDir.mkdirs();
        int files = Objects.requireNonNull(vouchDir.listFiles()).length;
        if (files >= 5) throw new AccessDeniedException("The voucher has already reached their vouch limit: " + voucher);
        File vouchFile = new File(vouchDir, voucher + "." + (files + 1) + "." + ".json");
        if (!vouchFile.createNewFile()) throw new IOException("Failed to create vouch");
        try (FileWriter writer = new FileWriter(vouchFile))
        {
            VouchRecord vouchRecord = new VouchRecord(vouched);
            gson.toJson(vouchRecord, writer);
        }
    }
}
