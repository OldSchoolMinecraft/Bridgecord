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
            if (Bukkit.getOnlinePlayers().length == 0)
            {
                event.getMessage().reply("Nobody is online :(").queue();
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
                event.getMessage().reply("Nobody is online :(").queue();
                return;
            }
            String pre = sb.toString().trim();
            event.getMessage().reply("Online players (" + Math.max(Bukkit.getOnlinePlayers().length - invisSub, 0) + "):\n`" + pre.substring(0, pre.length() - 1) + "`").queue();
            return;
        }

        if (linkConf.isEnabled() && event.getMessage().getContentStripped().startsWith(cmdPrefix + linkConf.getLabel()))
        {
            String[] args = event.getMessage().getContentStripped().split(" ");

            if (args.length-1 < 1)
            {
                event.getMessage().reply("You must specify a username! Example: `" + cmdPrefix + linkConf.getLabel() + " ExamplePlayer123`").queue();
                return;
            }

            String targetLinkName = args[1];

            LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
            if (data != null)
            {
                event.getMessage().reply("You are already linked to `" + data.username + "` on this server :)").queue();
                return;
            }

            String code = linkHandler.startLinkProcess(targetLinkName, event.getAuthor().getId());

            event.getMessage().reply("Success! We have dispatched a Direct Message.").queue();
            queueDM(event.getAuthor(), "You have successfully initiated the linking process! Please connect to the server with the username you provided (`" + targetLinkName + "`), and enter the following into the chat: `/dlink " + code + "`.\r\rIf the username shown in this message is not correct (and exact, it's case-sensitive!), please try linking again with the correct username.");
            return;
        }

        if (resetConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + resetConf.getLabel()))
        {
            LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
            if (data == null)
            {
                event.getMessage().reply("Your account is not linked! Please use `" + cmdPrefix + linkConf.getLabel() + "` first!").queue();
                return;
            }

            String newPassword = Util.generateSecurePassword(); // 12 char alphanumeric, no special characters
            try
            {
                Util.selectAuthPlugin().updatePassword(data.username, newPassword);
            } catch (AuthHandlerException ex) {
                event.getMessage().reply("An unknown error has occurred while attempting to reset your password. Please try again later.").queue();
                return;
            }

            event.getMessage().reply("Success! We have dispatched a Direct Message.").queue();
            queueDM(event.getAuthor(), "Your in-game account has been updated with a new randomly generated password: `" + newPassword + "`. While we do not store these passwords, it is still recommended that you change it to something else. For added security, a password manager is encouraged (but not *required*).");
            return;
        }

        if (authConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + authConf.getLabel()))
        {
            LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
            if (data == null)
            {
                event.getMessage().reply("Your account is not linked! Please use `" + cmdPrefix + linkConf.getLabel() + "` first!").queue();
                return;
            }

            try
            {
                Player player = Bukkit.getPlayer(data.username);

                if (player == null)
                {
                    event.getMessage().reply("You must be connected to the server to use this feature!").queue();
                    return;
                }

                String ip = ((CraftPlayer) player).getHandle().netServerHandler.networkManager.socket.getInetAddress().getHostAddress();
                Util.selectAuthPlugin().authenticate(data.username, ip);

                event.getMessage().reply("You have been successfully authorized in-game!").queue();
            } catch (AuthHandlerException e) {
                event.getMessage().reply("Error: " + e.getMessage()).queue();
            }

            return;
        }

        if (setNameConf.isEnabled() && event.getMessage().getContentStripped().equalsIgnoreCase(cmdPrefix + setNameConf.getLabel()))
        {
            boolean notImplemented = true;
            if (notImplemented)
            {
                event.getMessage().reply("This command is not yet implemented. Sorry!").queue();
                return;
            }

            LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
            if (data == null)
            {
                event.getMessage().reply("Your account is not linked! Please use `" + cmdPrefix + linkConf.getLabel() + "` first!").queue();
                return;
            }

            //TODO: create or update record with new display name
            //TODO: cache data for 45 minutes in memory
        }

        List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());
        // read bridge messages from all configured channel IDs
        if (channelIDs.contains(event.getChannel().getId())) //if (event.getChannel().getId().equals(String.valueOf(config.getConfigOption("bridgeChannelID"))))
        {
            ArrayList<String> msgChunks = new ArrayList<>();

            //TODO: check if user has custom display name, and process accordingly -- check color role config & validate

            String pre = Util.processMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(config.getConfigOption("bridgeMessageFormat.shownInGame"))), new HashMap<String, String>()
            {{
                put("{name}", event.getAuthor().getName());
                put("{displayName}", event.getAuthor().getName());
                put("{msg}", Util.stripUnprocessedColor(ChatColor.stripColor(event.getMessage().getContentStripped())));
            }});
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

    private void queueDM(User user, String message)
    {
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
    }
}
