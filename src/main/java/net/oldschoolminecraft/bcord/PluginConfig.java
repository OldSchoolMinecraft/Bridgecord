package net.oldschoolminecraft.bcord;

import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.Arrays;

public class PluginConfig extends Configuration
{
    public PluginConfig(File file)
    {
        super(file);
    }

    public void reload()
    {
        load();
        write();
        save();
    }

    public void write()
    {
        generateConfigOption("listCommandLabel", "list");
        generateConfigOption("bridgeChannelID", "INSERT_CHANNEL_ID");
        generateConfigOption("bridgeMessageFormat", "&8{name}:&f {msg}");
        generateConfigOption("hidePlayersWithPermission", "bcord.hidden");
        generateConfigOption("useInvisiman", false);
        generateConfigOption("preventUnauthorizedChats", true);
        generateConfigOption("escapeAtSymbols", true);
        getStringList("blockedKeywords", Arrays.asList("@everyone", "http://", "https://"));
    }

    private void generateConfigOption(String key, Object defaultValue)
    {
        if (this.getProperty(key) == null) this.setProperty(key, defaultValue);
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    public Object getConfigOption(String key)
    {
        return this.getProperty(key);
    }

    public Object getConfigOption(String key, Object defaultValue)
    {
        Object value = getConfigOption(key);
        if (value == null) value = defaultValue;
        return value;
    }
}
