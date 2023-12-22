package net.oldschoolminecraft.bcord.util;

import com.sun.scenario.effect.Brightpass;
import net.oldschoolminecraft.bcord.Bridgecord;
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
        getStringList("bridgeChannelIDs", Arrays.asList("FIRST_CHANNEL_ID", "SECOND_CHANNEL_ID", "THIRD_CHANNEL_ID?"));
        generateConfigOption("bridgeMessageFormat", "&8{name}:&f {msg}");

        generateConfigOption("bridgeMessageFormat.shownInGame", "&8{name}:&f {msg}");
        generateConfigOption("bridgeMessageFormat.shownInDiscord", "**<{name}>** {msg}");

        generateConfigOption("hidePlayersWithPermission", "bcord.hidden");
        generateConfigOption("useInvisiman", false);
        generateConfigOption("preventUnauthorizedChats", true);
        generateConfigOption("escapeAtSymbols", true);

        generateConfigOption("commands.prefix", "!");

        generateConfigOption("commands.list.enabled", true);
        generateConfigOption("commands.list.label", "list");

        generateConfigOption("commands.link.enabled", true);
        generateConfigOption("commands.link.label", "link");

        generateConfigOption("commands.reset.enabled", true);
        generateConfigOption("commands.reset.label", "reset");

        generateConfigOption("commands.auth.enabled", true);
        generateConfigOption("commands.auth.label", "auth");

        generateConfigOption("discordLinking.dataSource", "remote");
        generateConfigOption("discordLinking.DISCLAIMER_NOTE", "The dataSource value must be kept at 'remote' until the local data source is properly implemented. Thank you!");
        generateConfigOption("discordLinking.local.dataDirectory", Bridgecord.getInstance().getDataFolder().getAbsolutePath());
        generateConfigOption("discordLinking.remote.host", "127.0.0.1");
        generateConfigOption("discordLinking.remote.port", 3306);
        generateConfigOption("discordLinking.remote.username", "root");
        generateConfigOption("discordLinking.remote.password", "changeme");
        generateConfigOption("discordLinking.remote.database", "bridgecord");

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
