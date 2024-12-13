package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.johnymuffin.discordcore.DiscordCore;
import com.oldschoolminecraft.vanish.Invisiman;
import io.github.aleksandarharalanov.chatguard.ChatGuard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.oldschoolminecraft.bcord.cmd.BcordCommand;
import net.oldschoolminecraft.bcord.cmd.LinkCommandHandler;
import net.oldschoolminecraft.bcord.cmd.StaffLockCommand;
import net.oldschoolminecraft.bcord.cmd.bot.BotListener;
import net.oldschoolminecraft.bcord.data.AbstractDataSource;
import net.oldschoolminecraft.bcord.data.RemoteDataSource;
import net.oldschoolminecraft.bcord.event.BridgecordHandler;
import net.oldschoolminecraft.bcord.event.BukkitPlayerHandler;
import net.oldschoolminecraft.bcord.event.PoseidonPlayerHandler;
import net.oldschoolminecraft.bcord.hooks.CGUtils;
import net.oldschoolminecraft.bcord.hooks.EssUtils;
import net.oldschoolminecraft.bcord.hooks.OSMPLUtils;
import net.oldschoolminecraft.bcord.hooks.PEXUtils;
import net.oldschoolminecraft.bcord.util.*;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
    private PEXUtils pexUtils;
    private EssUtils essUtils;
    private OSMPLUtils osmplUtils;
    private BridgecordHandler currentEventHandler;
    private BotListener botListener;
    private CGUtils cgUtils;

    public void onEnable()
    {
        instance = this;
        config = new PluginConfig(new File(getDataFolder(), "config.yml"));
        config.reload();

        try
        {
            Long.valueOf(config.getString("primaryServerID"));
        } catch (Exception ex) {
            System.err.println(">> BRIDGECORD NOT ENABLED <<");
            System.err.println("You must set the 'primaryServerID' option in the config to a valid long.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DiscordCore core = (DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore");
        bot = core.getDiscordBot();

        try
        {
            pexUtils = new PEXUtils();
        } catch (NoClassDefFoundError ignored) {}

        try
        {
            essUtils = new EssUtils();
        } catch (NoClassDefFoundError ignored) {}

        try
        {
            osmplUtils = new OSMPLUtils();
        } catch (Exception ignored) {}

        try
        {
            cgUtils = new CGUtils();
        } catch (Exception ignored) {}

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
                String url = "jdbc:mysql://" + config.getConfigOption("discordLinking.remote.host") + ":" + config.getConfigOption("discordLinking.remote.port") + "/" + config.getConfigOption("discordLinking.remote.database") + "?characterEncoding=" + config.getConfigOption("discordLinking.remote.encoding", "latin1");
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

        this.botListener = new BotListener();
        bot.jda.addEventListener(botListener);

        registerEvents();

        getCommand("bcord").setExecutor(new BcordCommand());
        getCommand("dlink").setExecutor(new LinkCommandHandler());
        getCommand("slock").setExecutor(new StaffLockCommand());

        System.out.println("Bridgecord enabled");
    }

    public void registerEvents()
    {
        if (currentEventHandler != null) currentEventHandler.disable();
        boolean useSuperEvents = config.getBoolean("priority.useSuperEvents", false);
        currentEventHandler = useSuperEvents ? new PoseidonPlayerHandler() : new BukkitPlayerHandler();
        Event.Priority eventPriority = Event.Priority.valueOf(config.getString("priority.eventPriority", "Highest"));
        if (useSuperEvents)
            getServer().getPluginManager().registerSuperEvents(currentEventHandler, this);
        else {
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, currentEventHandler, eventPriority, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, currentEventHandler, eventPriority, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, currentEventHandler, eventPriority, this);
        }
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

    public PEXUtils getPexUtils()
    {
        return pexUtils;
    }

    public EssUtils getEssUtils()
    {
        return essUtils;
    }

    public OSMPLUtils getOSMPLUtils()
    {
        return osmplUtils;
    }

    public CGUtils getCgUtils()
    {
        return cgUtils;
    }

    public BridgecordHandler getCurrentEventHandler()
    {
        return currentEventHandler;
    }

    public BotListener getBotListener()
    {
        return botListener;
    }

    public void onDisable()
    {
        linkHandler.getDataSource().shutdown();
        System.out.println("Bridgecord disabled");
    }
}
