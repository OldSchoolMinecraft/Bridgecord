package net.oldschoolminecraft.bcord.event;

import com.johnymuffin.discordcore.DiscordBot;
import com.oldschoolminecraft.jp.JoinsPlus;
import com.oldschoolminecraft.jp.Message;
import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.entities.TextChannel;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.util.PluginConfig;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public abstract class BridgecordHandler extends PlayerListener
{
    private static final Bridgecord plugin = Bridgecord.getInstance();
    private static final DiscordBot bot = Bridgecord.getInstance().getBot();
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private static final List<String> blockedKeywords = new ArrayList<>();
    private boolean DISABLED = false;

    public void onPlayerChat(PlayerChatEvent event)
    {
        if (DISABLED) return;

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
                put("{name}", Util.stripAllColor(event.getPlayer().getName()));
                put("{displayName}", Util.stripAllColor(event.getPlayer().getDisplayName()));
                put("{msg}", Util.stripAllColor(event.getMessage()));
                if (config.getBoolean("usePEXPrefixes", false))
                {
                    put("{group}", Util.stripAllColor(plugin.getPexUtils().getFirstGroup(event.getPlayer().getName())));
                    put("{prefix}", Util.stripAllColor(plugin.getPexUtils().getWholePrefix(event.getPlayer().getName())));
                }
            }});
            deliverMessage(formattedMessage);
        }, 0L);
    }
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (DISABLED) return;

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
            deliverMessage(Util.stripAllColor(msg));
        }, 0L);
    }
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (DISABLED) return;

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
            deliverMessage(Util.stripAllColor(msg));
        }, 0L);
    }

    public void disable()
    {
        DISABLED = true;
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
