package net.oldschoolminecraft.bcord;

import com.avaje.ebeaninternal.server.core.Message;
import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BotListener extends ListenerAdapter
{
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();

    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
//        System.out.println("[Bridgecord] Message received from channel: " + event.getMessage().getChannel().getId());
//        System.out.println("[Bridgecord] Message content: " + event.getMessage().getContentRaw());
//        System.out.println("[Bridgecord] Is webhook message? " + (event.isWebhookMessage() ? "Yes" : "No"));
//        System.out.println("[Bridgecord] Is bot message? " + (event.getAuthor().isBot() ? "Yes" : "No"));
//        System.out.println("[Bridgecord] Is system message? " + (event.getAuthor().isSystem() ? "Yes" : "No"));

        if (!event.getAuthor().isBot() && !event.getAuthor().isSystem() && !event.getMessage().isWebhookMessage() && event.getMessage().getContentRaw().equalsIgnoreCase("!" + config.getConfigOption("listCommandLabel")))
        {
            if (Bukkit.getOnlinePlayers().length == 0)
            {
                event.getMessage().reply("Nobody is online :(").complete();
                return;
            }

            String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
            Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
            boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
            boolean invisimanInstalled = invisiman != null;
            StringBuilder sb = new StringBuilder();
            int invisSub = 0;
            for (Player p : Bukkit.getOnlinePlayers())
            {
                boolean playerIsVanished = (!useInvisiman && p.hasPermission(hideWithPerm)) || (useInvisiman && invisimanInstalled && invisiman.isVanished(p));
                if (!playerIsVanished) sb.append(p.getName()).append(", ");
                else invisSub++;
            }
            if (Bukkit.getOnlinePlayers().length - invisSub < 1)
            {
                event.getMessage().reply("Nobody is online :(").complete();
                return;
            }
            String pre = sb.toString().trim();
            event.getMessage().reply("Online players (" + Math.max(Bukkit.getOnlinePlayers().length - invisSub, 0) + "):\n`" + pre.substring(0, pre.length() - 1) + "`").complete();
            return;
        }

        if (event.getChannel().getId().equals(String.valueOf(config.getConfigOption("bridgeChannelID"))) && !event.isWebhookMessage() && !event.getAuthor().isBot() && !event.getAuthor().isSystem())
        {
            ArrayList<String> msgChunks = new ArrayList<>();
            String pre = ChatColor.translateAlternateColorCodes('&', String.valueOf(config.getConfigOption("bridgeMessageFormat")));
            pre = pre.replace("{name}", event.getAuthor().getName());
            pre = pre.replace("{msg}", Util.stripUnprocessedColor(ChatColor.stripColor(event.getMessage().getContentStripped())));
            if (pre.length() <= 128)
                msgChunks.add(pre);
            else {
                msgChunks.addAll(Util.splitString(pre, 128));
            }
            for (Player p : Bukkit.getOnlinePlayers())
                msgChunks.forEach(p::sendMessage);
            return;
        }
    }
}
