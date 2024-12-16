package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.requests.Route;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BotWhitelistCommand extends BotCommand
{
    public BotWhitelistCommand()
    {
        super("whitelist");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        String[] args = event.getMessage().getContentRaw().split(" ");

        List<String> allowedRoleIDs = config.getStringList("command.whitelist.allowedRoles", new ArrayList<>());
        if (allowedRoleIDs == null)
        {
            event.getMessage().reply("Failed to check permissions. Contact the system administrator.").queue();
            return;
        }

        boolean isAllowed = false;
        for (String roleID : allowedRoleIDs)
            if (hasRoleByID(event.getMember(), roleID))
                isAllowed = true;

        if (!isAllowed)
        {
            event.getMessage().reply("You do not have any of the required roles to use this command!").queue();
            return;
        }

        if (!Bukkit.getServer().hasWhitelist())
        {
            event.getMessage().reply("The whitelist is not currently active.").queue();
            return;
        }

        if (args.length-1 < 1)
        {
            event.getMessage().reply("You must specify a username! Usage: `!whitelist <username>`").queue();
            return;
        }

        String username = args[1];

        AuthPluginHandler authHandler = Util.selectAuthPlugin();
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        if ((player == null || !authHandler.isRegistered(username)) && !(hasRoleByID(event.getMember(), String.valueOf(config.getConfigOption("adminRoleID")))))
        {
            event.getMessage().reply("Only administrators can whitelist this user, as they have never played before.").queue();
            return;
        }

        if (player == null)
        {
            event.getMessage().reply("An error occurred while trying to whitelist this player.").queue();
            return;
        }

        player.setWhitelisted(true);
        event.getMessage().addReaction("✅").queue();
    }
}
