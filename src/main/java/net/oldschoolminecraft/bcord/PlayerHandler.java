package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.oldschoolminecraft.jp.JoinsPlus;
import com.oldschoolminecraft.jp.Message;
import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.entities.TextChannel;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.util.PluginConfig;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class PlayerHandler implements Listener
{
    private static final DiscordBot bot = Bridgecord.getInstance().getBot();
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private static final List<String> blockedKeywords = new ArrayList<>();

    private Bridgecord plugin;
    private final List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());

    public PlayerHandler(Bridgecord plugin)
    {
        this.plugin = plugin;

        blockedKeywords.addAll(config.getStringList("blockedKeywords", Collections.singletonList("@everyone")));
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

            for (String keyword : blockedKeywords)
            {
                if (event.getMessage().contains(keyword))
                {
                    event.getPlayer().sendMessage(ChatColor.RED + "Your message contained a blocked keyword, and was not sent to the chat bridge.");
                    return;
                }
            }

            String formattedMessage = Util.processMessage(String.valueOf(config.getConfigOption("bridgeMessageFormat.shownInDiscord")), new HashMap<String, String>()
            {{
                put("{name}", event.getPlayer().getName());
                put("{displayName}", event.getPlayer().getDisplayName());
                put("{msg}", event.getMessage());
            }});
            deliverMessage(Util.stripUnprocessedColor(formattedMessage));
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
                Message jpMessage = jp.loadMessage(event.getPlayer().getName());
                if (jpMessage != null)
                {
                    msg = "*" + jpMessage.join + "*";
                    msg = msg.replace("%player%", "__" + event.getPlayer().getName() + "__");
                }
            }
            if (invisimanInstalled && useInvisiman && invisiman.isVanished(event.getPlayer())) return;
            if (event.getPlayer().hasPermission(hideWithPerm)) return;
            deliverMessage(Util.stripUnprocessedColor(msg));
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
                Message jpMessage = jp.loadMessage(event.getPlayer().getName());
                if (jpMessage != null)
                {
                    msg = "*" + jpMessage.quit + "*";
                    msg = msg.replace("%player%", "__" + event.getPlayer().getName() + "__");
                }
            }
            if (invisimanInstalled && useInvisiman && invisiman.isVanished(event.getPlayer())) return;
            if (event.getPlayer().hasPermission(hideWithPerm)) return;
            deliverMessage(Util.stripUnprocessedColor(msg));
        }, 0L);
    }

    private void deliverMessage(String message)
    {
        List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());
        for (String channelID : channelIDs)
        {
            TextChannel channel = Bridgecord.getInstance().getBot().jda.getTextChannelById(channelID);
            if (channel == null)
            {
                System.out.println("[Bridgecord] Failed to get JDA handle for text channel: " + channelID);
                continue;
            }
            Objects.requireNonNull(channel).sendMessage(message).queue();
        }
    }
}
