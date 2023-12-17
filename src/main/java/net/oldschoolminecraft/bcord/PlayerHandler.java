package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.oldschoolminecraft.jp.JoinsPlus;
import com.oldschoolminecraft.vanish.Invisiman;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;

public class PlayerHandler implements Listener
{
    private static final DiscordBot bot = Bridgecord.getInstance().getBot();
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();

    private Bridgecord plugin;

    public PlayerHandler(Bridgecord plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event)
    {
        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            boolean preventUnauthorizedChats = (boolean) config.getConfigOption("preventUnauthorizedChats");
            if (preventUnauthorizedChats)
            {
                AuthPluginHandler authHandler = Util.selectAuthPlugin();
                if (!authHandler.isInstalled()) return;
                if (!authHandler.isAuthorized(event.getPlayer().getName())) return;
            }
            String msg = "**<" + event.getPlayer().getName() + ">** " + event.getMessage();
            Objects.requireNonNull(bot.jda.getTextChannelById(String.valueOf(config.getConfigOption("bridgeChannelID")))).sendMessage(msg).complete();
        }, 0L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            JoinsPlus jp = (JoinsPlus) Bukkit.getServer().getPluginManager().getPlugin("JoinsPlus");
            Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
            boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
            boolean invisimanInstalled = invisiman != null;
            String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
            String msg = "*__" + event.getPlayer().getName() + "__ has connected*";
            if (jp != null)
            {
                msg = "*" + jp.loadMessage(event.getPlayer().getName()).join + "*";
                msg = msg.replace("%player%", "__" + event.getPlayer().getName() + "__");
            }
            if (invisimanInstalled && useInvisiman && invisiman.isVanished(event.getPlayer())) return;
            if (event.getPlayer().hasPermission(hideWithPerm)) return;
            Objects.requireNonNull(bot.jda.getTextChannelById(String.valueOf(config.getConfigOption("bridgeChannelID")))).sendMessage(Util.stripUnprocessedColor(msg)).complete();
        }, 0L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            JoinsPlus jp = (JoinsPlus) Bukkit.getServer().getPluginManager().getPlugin("JoinsPlus");
            Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
            boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
            boolean invisimanInstalled = invisiman != null;
            String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
            String msg = "*__" + event.getPlayer().getName() + "__ has disconnected*";
            if (jp != null)
            {
                msg = "*" + jp.loadMessage(event.getPlayer().getName()).quit + "*";
                msg = msg.replace("%player%", "__" + event.getPlayer().getName() + "__");
            }
            if (invisimanInstalled && useInvisiman && invisiman.isVanished(event.getPlayer())) return;
            if (event.getPlayer().hasPermission(hideWithPerm)) return;
            Objects.requireNonNull(bot.jda.getTextChannelById(String.valueOf(config.getConfigOption("bridgeChannelID")))).sendMessage(Util.stripUnprocessedColor(msg)).complete();
        }, 0L);
    }


}
