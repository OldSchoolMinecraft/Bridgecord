package net.oldschoolminecraft.bcord;

import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.oldschoolminecraft.bcord.auth.AuthHandlerException;
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
    private final BotCommandConfig listConf = new BotCommandConfig(config, "list");
    private final BotCommandConfig linkConf = new BotCommandConfig(config, "link");
    private final BotCommandConfig resetConf = new BotCommandConfig(config, "reset");
    private final BotCommandConfig authConf = new BotCommandConfig(config, "auth");
    private final BotCommandConfig setNameConf = new BotCommandConfig(config, "setname");
    private final DiscordLinkHandler linkHandler = Bridgecord.getInstance().getLinkHandler();

    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().isSystem()) return;
        if (event.getMessage().isWebhookMessage()) return;

        if (listConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + listConf.getLabel()))
        {
            asyncImmediately(() ->
            {
                if (Bukkit.getOnlinePlayers().length == 0)
                {
                    respond(event.getMessage(), String.valueOf(config.getConfigOption("serverEmptyMessage")), listConf.shouldReply());
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
                    respond(event.getMessage(), String.valueOf(config.getConfigOption("serverEmptyMessage")), listConf.shouldReply());
                    return;
                }
                String pre = sb.toString().trim();
                respond(event.getMessage(), "Online players (" + Math.max(Bukkit.getOnlinePlayers().length - invisSub, 0) + "):\n`" + pre.substring(0, pre.length() - 1) + "`", listConf.shouldReply());
            });
            return;
        }

        if (linkConf.isEnabled() && event.getMessage().getContentStripped().startsWith(cmdPrefix + linkConf.getLabel()))
        {
            asyncImmediately(() ->
            {
                String[] args = event.getMessage().getContentStripped().split(" ");

                if (args.length-1 < 1)
                {
                    respond(event.getMessage(), "You must specify a username! Example: `" + cmdPrefix + linkConf.getLabel() + " ExamplePlayer123`", linkConf.shouldReply());
                    return;
                }

                String targetLinkName = args[1];

                LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
                if (data != null)
                {
                    respond(event.getMessage(), "You are already linked to `" + data.username + "` on this server :)", linkConf.shouldReply());
                    return;
                }

                String code = linkHandler.startLinkProcess(targetLinkName, event.getAuthor().getId());

                respond(event.getMessage(), "Success! We have dispatched a Direct Message.", linkConf.shouldReply());
                queueDM(event.getAuthor(), "You have successfully initiated the linking process! Please connect to the server with the username you provided (`" + targetLinkName + "`), and enter the following into the chat: `/dlink " + code + "`.\r\rIf the username shown in this message is not correct (and exact, it's case-sensitive!), please try linking again with the correct username.");
            });
            return;
        }

        if (resetConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + resetConf.getLabel()))
        {
            asyncImmediately(() ->
            {
                LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
                if (data == null)
                {
                    respond(event.getMessage(), "Your account is not linked! Please use `" + cmdPrefix + linkConf.getLabel() + "` first!", resetConf.shouldReply());
                    return;
                }

                String newPassword = Util.generateSecurePassword(); // 12 char alphanumeric, no special characters
                try
                {
                    Util.selectAuthPlugin().updatePassword(data.username, newPassword);
                } catch (AuthHandlerException ex) {
                    respond(event.getMessage(), "An unknown error has occurred while attempting to reset your password. Please try again later.", resetConf.shouldReply());
                    return;
                }

                respond(event.getMessage(), "Success! We have dispatched a Direct Message.", resetConf.shouldReply());
                queueDM(event.getAuthor(), "Your in-game account has been updated with a new randomly generated password: `" + newPassword + "`. While we do not store these passwords, it is still recommended that you change it to something else. For added security, a password manager is encouraged (but not *required*).");
            });
            return;
        }

        if (authConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + authConf.getLabel()))
        {
            asyncImmediately(() ->
            {
                LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
                if (data == null)
                {
                    respond(event.getMessage(), "Your account is not linked! Please use `" + cmdPrefix + linkConf.getLabel() + "` first!", authConf.shouldReply());
                    return;
                }

                try
                {
                    Player player = Bukkit.getPlayer(data.username);

                    if (player == null)
                    {
                        respond(event.getMessage(), "You must be connected to the server to use this feature!", authConf.shouldReply());
                        return;
                    }

                    String ip = ((CraftPlayer) player).getHandle().netServerHandler.networkManager.socket.getInetAddress().getHostAddress();
                    Util.selectAuthPlugin().authenticate(data.username, ip);

                    respond(event.getMessage(), "You have been successfully authorized in-game!", authConf.shouldReply());
                } catch (AuthHandlerException e) {
                    respond(event.getMessage(), "Error: " + e.getMessage(), authConf.shouldReply());
                }
            });

            return;
        }

        if (setNameConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + setNameConf.getLabel()))
        {
            asyncImmediately(() ->
            {
                boolean notImplemented = true;
                if (notImplemented)
                {
                    respond(event.getMessage(), "This command is not yet implemented. Sorry!", setNameConf.shouldReply());
                    return;
                }

                LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
                if (data == null)
                {
                    respond(event.getMessage(), "Your account is not linked! Please use `" + cmdPrefix + linkConf.getLabel() + "` first!", setNameConf.shouldReply());
                    return;
                }

                //TODO: create or update record with new display name
                //TODO: cache data for 45 minutes in memory
            });

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
//            return;
        }
    }

    private void respond(Message dcMsg, String replyMsg, boolean reply)
    {
        if (reply)
            dcMsg.reply(replyMsg).queue();
        else dcMsg.getChannel().sendMessage(replyMsg).queue();
    }

    private void asyncImmediately(Runnable runnable)
    {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Bridgecord.getInstance(), runnable, 0L);
    }

    private void queueDM(User user, String message)
    {
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
    }
}
