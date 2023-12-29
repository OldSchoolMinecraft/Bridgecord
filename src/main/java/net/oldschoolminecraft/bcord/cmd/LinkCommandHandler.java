package net.oldschoolminecraft.bcord.cmd;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.Bukkit;
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
            Bukkit.getScheduler().scheduleAsyncDelayedTask(Bridgecord.getInstance(), () ->
            {
                if (args.length < 1)
                {
                    sender.sendMessage(ChatColor.RED + "Insufficient arguments: Usage: /dlink <code>");
                    return;
                }

                if (!(sender instanceof Player))
                {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                    return;
                }

                try
                {
                    String code = args[0];

                    if (Bridgecord.getInstance().getLinkHandler().completeLinkProcess(sender.getName(), code))
                    {
                        sender.sendMessage(ChatColor.GREEN + "Your account has been linked successfully!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Link failed. Your username or the code provided did not match our records.");
                    }
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Link error: " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
            }, 0L);

            return true;
        }

        return true;
    }
}
