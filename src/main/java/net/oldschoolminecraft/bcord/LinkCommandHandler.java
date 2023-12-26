package net.oldschoolminecraft.bcord;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkCommandHandler implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (label.equalsIgnoreCase("dlink"))
        {
            if (args.length < 1)
            {
                sender.sendMessage(ChatColor.RED + "Insufficient arguments: Usage: /dlink <code>");
                return true;
            }

            if (!(sender instanceof Player))
            {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                return true;
            }

            String code = args[0];

            if (Bridgecord.getInstance().getLinkHandler().completeLinkProcess(sender.getName(), code))
            {
                sender.sendMessage(ChatColor.GREEN + "Your account has been linked successfully!");
            } else {
                sender.sendMessage(ChatColor.RED + "Link failed. Your username or the code provided did not match our records.");
            }

            return true;
        }

        return true;
    }
}
