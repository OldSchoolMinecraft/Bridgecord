package net.oldschoolminecraft.bcord.cmd;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BcordCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (label.equalsIgnoreCase("bcord"))
        {
            if (args.length >= 1 && args[0].equalsIgnoreCase("reload"))
            {
                if (!(sender.hasPermission("bcord.admin") || sender.isOp()))
                {
                    sender.sendMessage(ChatColor.RED + "No permission");
                    return true;
                }

                Bridgecord.getInstance().getConfig().reload();
                Bridgecord.getInstance().registerEvents();
                sender.sendMessage(ChatColor.GREEN + "Bridgecord configuration & event handlers reloaded");
            } else if (args.length == 0) {
                sender.sendMessage(ChatColor.GRAY + "Bridgecord v" + Bridgecord.getInstance().getDescription().getVersion() + " by " + ChatColor.RED + "moderator_man");
            }
        }

        return true;
    }
}
