package net.oldschoolminecraft.bcord.util;

import com.oldschoolminecraft.vanish.Invisiman;
import com.sun.scenario.effect.Brightpass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        //TODO: combine channel IDs and messages into object list so each channel can have it's own format (for inter-server chat that use differing formats)
        generateConfigOption("bridgeChannelIDs", Arrays.asList("FIRST_CHANNEL_ID", "SECOND_CHANNEL_ID", "THIRD_CHANNEL_ID?"));
        generateConfigOption("bridgeMessageFormat", "&8{name}:&f {msg}");

        generateConfigOption("bridgeMessageFormat.shownInGame", "&8{name}:&f {msg}");
        generateConfigOption("bridgeMessageFormat.shownInDiscord", "**<{name}>** {msg}");

        generateConfigOption("hidePlayersWithPermission", "bcord.hidden");
        generateConfigOption("useInvisiman", false);
        generateConfigOption("usePEXPrefixes", false);
        generateConfigOption("preventUnauthorizedChats", true);

        generateConfigOption("commands.prefix", "!");

        generateConfigOption("discordLinking.dataSource", "remote");
        generateConfigOption("discordLinking.DISCLAIMER_NOTE", "The dataSource value must be kept at 'remote' until the local data source is properly implemented. Thank you!");
        generateConfigOption("discordLinking.local.dataDirectory", Bridgecord.getInstance().getDataFolder().getAbsolutePath());
        generateConfigOption("discordLinking.remote.host", "127.0.0.1");
        generateConfigOption("discordLinking.remote.port", 3306);
        generateConfigOption("discordLinking.remote.username", "root");
        generateConfigOption("discordLinking.remote.password", "changeme");
        generateConfigOption("discordLinking.remote.database", "bridgecord");
        generateConfigOption("discordLinking.customProfiles.info_note", "This feature allows players to set their bridge name to something other than their Discord name");
        generateConfigOption("discordLinking.customProfiles.enabled", true);
        generateConfigOption("discordLinking.customProfiles.useColorRole", false);
        generateConfigOption("discordLinking.customProfiles.colorRoleID", "INSERT_ROLE_ID_HERE");

        generateConfigOption("listEmbedFormatFile", "fancy_list.json");

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
