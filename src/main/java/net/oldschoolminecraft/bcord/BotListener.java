package net.oldschoolminecraft.bcord;

import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.oldschoolminecraft.bcord.auth.AuthHandlerException;
import net.oldschoolminecraft.bcord.cmd.bot.*;
import net.oldschoolminecraft.bcord.data.RemoteDataSource;
import net.oldschoolminecraft.bcord.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BotListener extends ListenerAdapter
{
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();
    private final String cmdPrefix = String.valueOf(config.getConfigOption("commands.prefix"));
    private final ArrayList<BotCommand> botCommands = new ArrayList<>();

    public BotListener()
    {
        botCommands.add(new BotListCommand());
        botCommands.add(new BotLinkCommand());
        botCommands.add(new BotResetCommand());
        botCommands.add(new BotAuthCommand());
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().isSystem()) return;
        if (event.getMessage().isWebhookMessage()) return;

        String strippedMsg = event.getMessage().getContentStripped();
        if (strippedMsg.startsWith(cmdPrefix))
        {
            boolean hasArgs = strippedMsg.contains(" ");
            String[] parts = hasArgs ? strippedMsg.split(" ") : new String[] { strippedMsg };
            for (BotCommand cmd : botCommands)
            {
                if (cmd.getConfig().isPrimaryServerOnly() && !event.getGuild().getId().equals(String.valueOf(config.getConfigOption("primaryServerID")))) continue;
                if (cmd.getConfig().isEnabled() && parts[0].equalsIgnoreCase(cmdPrefix + cmd.getConfig().getLabel()))
                {
                    asyncImmediately(() -> cmd.execute(event));
                    return;
                }
            }

            respond(event.getMessage(), "No command exists with that name.", true);
            return;
        }

        List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());
        // read bridge messages from all configured channel IDs
        if (channelIDs.contains(event.getChannel().getId())) //if (event.getChannel().getId().equals(String.valueOf(config.getConfigOption("bridgeChannelID"))))
        {
            asyncImmediately(() ->
            {
                ArrayList<String> msgChunks = new ArrayList<>();

                //TODO: check if user has custom display name, and process accordingly -- check color role config & validate

                String pre = Util.processMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(config.getConfigOption("bridgeMessageFormat.shownInGame"))), new HashMap<String, String>()
                {{
                    put("{name}", event.getAuthor().getName());
                    put("{displayName}", event.getAuthor().getName());
                    put("{msg}", Util.stripAllColor(event.getMessage().getContentStripped()));
                }});
                if (pre.length() <= 128)
                    msgChunks.add(pre);
                else {
                    msgChunks.addAll(Util.splitString(pre, 128));
                }
                for (Player p : Bukkit.getOnlinePlayers())
                    msgChunks.forEach(p::sendMessage);
            });
        }
    }

    public void respond(Message dcMsg, String replyMsg, boolean reply)
    {
        if (reply)
            dcMsg.reply(replyMsg).queue();
        else dcMsg.getChannel().sendMessage(replyMsg).queue();
    }

    private void asyncImmediately(Runnable runnable)
    {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Bridgecord.getInstance(), runnable, 0L);
    }
}
