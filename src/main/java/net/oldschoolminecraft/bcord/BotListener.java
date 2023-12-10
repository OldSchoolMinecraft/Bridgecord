package net.oldschoolminecraft.bcord;

import com.avaje.ebeaninternal.server.core.Message;
import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BotListener extends ListenerAdapter
{
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();

    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getChannel().getId().equals(config.getConfigOption("bridgeChannelID")) && !event.isWebhookMessage() && !event.getAuthor().isBot() && !event.getAuthor().isSystem())
        {
            String pre = ChatColor.translateAlternateColorCodes('&', String.valueOf(config.getConfigOption("bridgeMessageFormat")));
            pre = pre.replace("{name}", event.getAuthor().getName());
            pre = pre.replace("{msg}", event.getMessage().getContentStripped());
            if (pre.length() > 100)
            {
                event.getMessage().reply("**Your message can't be longer than 100 characters!**").complete();
                return;
            }
            for (Player p : Bukkit.getOnlinePlayers())
                p.sendMessage(pre);
        }

        if (!event.getAuthor().isBot() && !event.getAuthor().isSystem() && !event.getMessage().isWebhookMessage() && event.getMessage().getContentRaw().equalsIgnoreCase("!" + config.getConfigOption("listCommandLabel")))
        {
            String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
            Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
            boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
            boolean invisimanInstalled = invisiman != null;
            StringBuilder sb = new StringBuilder();
            for (Player p : Bukkit.getOnlinePlayers())
            {
                boolean playerIsVanished = (!useInvisiman && p.hasPermission(hideWithPerm)) || (useInvisiman && invisimanInstalled && invisiman.isVanished(p));
                if (playerIsVanished) sb.append(p.getName()).append(", ");
            }
            String pre = sb.toString();
            event.getMessage().reply("Online players (" + Bukkit.getOnlinePlayers().length + "):\n`" + pre.trim().substring(0, pre.length() - 1) + "`").complete();
        }
    }
}
