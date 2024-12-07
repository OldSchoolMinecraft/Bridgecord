package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.hooks.OSMPLUtils;
import net.oldschoolminecraft.bcord.util.LinkData;
import net.oldschoolminecraft.bcord.util.StaffLockHandler;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.TimeUnit;

public class BotVouchCommand extends BotCommand
{
    public BotVouchCommand()
    {
        super("vouch");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        String[] args = event.getMessage().getContentRaw().split(" ");

        if (!Bukkit.getServer().hasWhitelist())
        {
            event.getMessage().reply("The whitelist is not currently active.").queue();
            return;
        }

        if (args.length-1 < 1)
        {
            event.getMessage().reply("You must specify a username!").queue();
            return;
        }

        String username = args[1];

        LinkData linkData = Bridgecord.getInstance().getLinkHandler().loadLinkDataByID(event.getAuthor().getId());
        if (linkData == null)
        {
            event.getMessage().reply("Your account must be linked in order to vouch for players.").queue();
            return;
        }

        long playTime = new OSMPLUtils().getUserData(username).playTime;
        long hours = TimeUnit.MILLISECONDS.toHours(playTime);

        if (hours < 1)
        {
            event.getMessage().reply("You must have an hour of playtime to vouch for players!").queue();
            return;
        }

        if (linkData.username.equalsIgnoreCase(username))
        {
            event.getMessage().reply("You cannot vouch for yourself!").queue();
            return;
        }

        System.out.println("[Bridgecord] " + event.getAuthor().getName() + " (" + event.getAuthor().getId() + ") has vouched for player: " + username);

        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        player.setWhitelisted(true);
        event.getMessage().reply("Your vouch has been accepted and `" + player.getName() + "` has been whitelisted.").queue();
    }
}