package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.auth.AuthHandlerException;
import net.oldschoolminecraft.bcord.util.DiscordLinkHandler;
import net.oldschoolminecraft.bcord.util.LinkData;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BotAuthCommand extends BotCommand
{
    public BotAuthCommand()
    {
        super("auth");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        DiscordLinkHandler linkHandler = Bridgecord.getInstance().getLinkHandler();
        LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
        if (data == null)
        {
            respond(event.getMessage(), "Your account is not linked! Please use `" + cmdPrefix + config.getString("commands.link.label") + "` first!", getConfig().shouldReply());
            return;
        }

        try
        {
            Player player = Bukkit.getPlayer(data.username);

            if (player == null)
            {
                respond(event.getMessage(), "You must be connected to the server to use this feature!", getConfig().shouldReply());
                return;
            }

            String ip = ((CraftPlayer) player).getHandle().netServerHandler.networkManager.socket.getInetAddress().getHostAddress();
            Util.selectAuthPlugin().authenticate(data.username, ip);

            respond(event.getMessage(), "You have been successfully authorized in-game!", getConfig().shouldReply());
        } catch (AuthHandlerException e) {
            respond(event.getMessage(), "Error: " + e.getMessage(), getConfig().shouldReply());
        }
    }
}
