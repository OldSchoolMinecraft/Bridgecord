package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.johnymuffin.discordcore.DiscordCore;
import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.oldschoolminecraft.bcord.data.AbstractDataSource;
import net.oldschoolminecraft.bcord.data.RemoteDataSource;
import net.oldschoolminecraft.bcord.util.DiscordLinkHandler;
import net.oldschoolminecraft.bcord.util.MySQLConnectionManager;
import net.oldschoolminecraft.bcord.util.PluginConfig;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        {
            getServer().getPluginManager().disablePlugin(this);
            throw new NotImplementedException("Bridgecord does not currently support local data sources. Sorry! (please set discordLinking.dataSource to 'remote')");
        }
        else if (dataSourceType.equalsIgnoreCase("remote")) {
            try
            {
                String url = "jdbc:mysql://" + config.getConfigOption("discordLinking.remote.host") + ":" + config.getConfigOption("discordLinking.remote.port") + "/" + config.getConfigOption("discordLinking.remote.database");
                String user = String.valueOf(config.getConfigOption("discordLinking.remote.username"));
                String password = String.valueOf(config.getConfigOption("discordLinking.remote.password"));
                dataSource = new RemoteDataSource(new MySQLConnectionManager(url, user, password));
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
        getCommand("dlink").setExecutor(new LinkCommandHandler());

        System.out.println("Bridgecord enabled");
    }

    private MessageEmbed makeFancyList()
    {
        EmbedBuilder eb = new EmbedBuilder().setTitle("Online Players");
        String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
        Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
        boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
        boolean invisimanInstalled = invisiman != null;
        int invisSub = 0;
        for (Player p : Bukkit.getOnlinePlayers())
        {
            boolean playerIsVanished = (!useInvisiman && p.hasPermission(hideWithPerm)) || (useInvisiman && invisimanInstalled && invisiman.isVanished(p));
            if (!playerIsVanished) eb.appendDescription(p.getName()).appendDescription(", ");
            else invisSub++;
        }

        String pre = eb.getDescriptionBuilder().toString().trim();
        String desc = pre.substring(0, pre.length() - 1);
        eb.setDescription(desc);

        return eb.build();
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
