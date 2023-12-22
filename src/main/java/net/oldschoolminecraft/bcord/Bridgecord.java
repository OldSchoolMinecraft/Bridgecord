package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.johnymuffin.discordcore.DiscordCore;
import net.oldschoolminecraft.bcord.data.AbstractDataSource;
import net.oldschoolminecraft.bcord.data.LocalDataSource;
import net.oldschoolminecraft.bcord.data.RemoteDataSource;
import net.oldschoolminecraft.bcord.util.DiscordLinkHandler;
import net.oldschoolminecraft.bcord.util.MySQLConnectionPool;
import net.oldschoolminecraft.bcord.util.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Bridgecord extends JavaPlugin
{
    private static Bridgecord instance;
    public static Bridgecord getInstance()
    {
        return instance;
    }

    private PluginConfig config;
    private DiscordBot bot;
    private DiscordLinkHandler linkHandler;

    public void onEnable()
    {
        instance = this;
        config = new PluginConfig(new File(getDataFolder(), "config.yml"));
        config.reload();
        DiscordCore core = (DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore");
        bot = core.getDiscordBot();
        AbstractDataSource dataSource = null;
        String dataSourceType = String.valueOf(config.getConfigOption("discordLinking.dataSource"));
        if (dataSourceType.equalsIgnoreCase("local"))
            dataSource = new LocalDataSource(new File(String.valueOf(config.getConfigOption("discordLinking.local.dataDirectory"))));
        else if (dataSourceType.equalsIgnoreCase("remote")) {
            try
            {
                String url = "mysql://" + config.getConfigOption("discordLinking.remote.host") + ":" + config.getConfigOption("discordLinking.remote.port") + "/" + config.getConfigOption("discordLinking.remote.database");
                String user = String.valueOf(config.getConfigOption("discordLinking.remote.username"));
                String password = String.valueOf(config.getConfigOption("discordLinking.remote.password"));
                dataSource = new RemoteDataSource(new MySQLConnectionPool(url, user, password));
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                System.out.println("[Bridgecord] Failed to load MySQL data source");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        linkHandler = new DiscordLinkHandler(this, dataSource);

        bot.jda.addEventListener(new BotListener());
        getServer().getPluginManager().registerEvents(new PlayerHandler(this), this);

        System.out.println("Bridgecord enabled");
    }

    public DiscordLinkHandler getLinkHandler()
    {
        return linkHandler;
    }

    public DiscordBot getBot()
    {
        return bot;
    }

    public PluginConfig getConfig()
    {
        return config;
    }

    public void onDisable()
    {
        System.out.println("Bridgecord disabled");
    }
}
