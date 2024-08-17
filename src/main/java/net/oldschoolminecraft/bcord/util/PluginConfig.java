package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.event.Event;
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
        generateConfigOption("bridgeChannelIDs", Arrays.asList("FIRST_CHANNEL_ID", "SECOND_CHANNEL_ID", "THIRD_CHANNEL_ID?"));
        generateConfigOption("bridgeMessageFormat", "&8{name}:&f {msg}");

        generateConfigOption("bridgeMessageFormat.shownInGame", "&8{name}:&f {msg}");
        generateConfigOption("bridgeMessageFormat.shownInDiscord", "**<{name}>** {msg}");
        generateConfigOption("bridgeMessageFormat.statsOnJoinLeave", false);
        generateConfigOption("bridgeMessageFormat.statsFormat", "**[{online}/{maxPlayers}]**");
        //TODO: separate linked & unlinked formats

        generateConfigOption("staffRoleID", "INSERT_STAFF_ROLE_ID_HERE");
        generateConfigOption("deathMessagesOnBridge", true);
        generateConfigOption("hidePlayersWithPermission", "bcord.hidden");
        generateConfigOption("useInvisiman", false);
        generateConfigOption("usePEXPrefixes", false);
        generateConfigOption("checkEssentialsMutes", true);
        generateConfigOption("preventUnauthorizedChats", true);
        generateConfigOption("primaryServerID", "INSERT_SERVER_ID_HERE");
        generateConfigOption("hackyRegexFix", "\\[\\w+(?:\\. \\w+)?\\] \\w+§r§r: ");

        generateConfigOption("priority.useSuperEvents", false);
        generateConfigOption("priority.eventPriority", Event.Priority.Highest.toString());
        generateConfigOption("priority.info", "Use these to assist in compatibility with chat formatting & muting plugins");

        generateConfigOption("commands.prefix", "!");
        generateConfigOption("commands.link.label", "link");
        generateConfigOption("commands.auth.label", "auth");
        generateConfigOption("commands.reset.label", "reset");

        generateConfigOption("serverEmptyMessage", "Nobody is online :(");

        generateConfigOption("discordLinking.dataSource", "remote");
        generateConfigOption("discordLinking.enabled", true);
        generateConfigOption("discordLinking.DISCLAIMER_NOTE", "The dataSource value must be kept at 'remote' until the local data source is properly implemented. Thank you!");
        generateConfigOption("discordLinking.local.dataDirectory", Bridgecord.getInstance().getDataFolder().getAbsolutePath());
        generateConfigOption("discordLinking.remote.host", "127.0.0.1");
        generateConfigOption("discordLinking.remote.port", 3306);
        generateConfigOption("discordLinking.remote.username", "root");
        generateConfigOption("discordLinking.remote.password", "changeme");
        generateConfigOption("discordLinking.remote.database", "bridgecord");
        generateConfigOption("discordLinking.remote.encoding", "latin1");
        generateConfigOption("discordLinking.customProfiles.info_note", "This feature allows players to set their bridge name to something other than their Discord name");
        generateConfigOption("discordLinking.customProfiles.enabled", true);
        generateConfigOption("discordLinking.customProfiles.useColorRole", false);
        generateConfigOption("discordLinking.customProfiles.colorRoleID", "INSERT_ROLE_ID_HERE");

        generateConfigOption("blockedKeywords", Arrays.asList("@everyone", "@here", "http://", "https://"));
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
