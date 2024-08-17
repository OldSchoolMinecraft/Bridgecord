package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;

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

        AuthPluginHandler authHandler = Util.selectAuthPlugin();
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        if (player == null || !authHandler.isRegistered(username))
        {
            event.getMessage().reply("We are not currently accepting new players. Sorry!").queue();
            return;
        }

        player.setWhitelisted(true);
        event.getMessage().reply("You have been added to the whitelist!").queue();
    }
}
