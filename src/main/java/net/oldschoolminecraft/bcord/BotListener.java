package net.oldschoolminecraft.bcord;

import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.oldschoolminecraft.bcord.util.BotCommandConfig;
import net.oldschoolminecraft.bcord.util.PluginConfig;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BotListener extends ListenerAdapter
{
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();

    private final String cmdPrefix = String.valueOf(config.getConfigOption("commands.prefix"));
    private final BotCommandConfig listConf = new BotCommandConfig(config, "list");
    private final BotCommandConfig linkConf = new BotCommandConfig(config, "link");
    private final BotCommandConfig resetConf = new BotCommandConfig(config, "reset");

    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().isSystem()) return;
        if (event.getMessage().isWebhookMessage()) return;

        if (listConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + listConf.getLabel()))
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

        if (linkConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + linkConf.getLabel()))
        {
            //
        }

        if (resetConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + resetConf.getLabel()))
        {
            //
        }

        List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());
        // read bridge messages from all configured channel IDs
        if (channelIDs.contains(event.getChannel().getId())) //if (event.getChannel().getId().equals(String.valueOf(config.getConfigOption("bridgeChannelID"))))
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
            Objects.requireNonNull(channel).sendMessage(message).complete();
        }
    }
}
