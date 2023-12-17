package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.oldschoolminecraft.jp.JoinsPlus;
import com.oldschoolminecraft.vanish.Invisiman;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerHandler implements Listener
{
    private static final DiscordBot bot = Bridgecord.getInstance().getBot();
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event)
    {
        String msg = "**<" + event.getPlayer().getName() + ">** " + event.getMessage();
        Objects.requireNonNull(bot.jda.getTextChannelById(String.valueOf(config.getConfigOption("bridgeChannelID")))).sendMessage(msg).complete();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
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
        Objects.requireNonNull(bot.jda.getTextChannelById(String.valueOf(config.getConfigOption("bridgeChannelID")))).sendMessage(ChatColor.stripColor(msg)).complete();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
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
        Objects.requireNonNull(bot.jda.getTextChannelById(String.valueOf(config.getConfigOption("bridgeChannelID")))).sendMessage(ChatColor.stripColor(msg)).complete();
    }
}
