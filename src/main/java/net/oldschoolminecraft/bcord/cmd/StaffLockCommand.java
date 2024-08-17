package net.oldschoolminecraft.bcord.cmd;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class StaffLockCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender.hasPermission("bcord.slock") || sender.isOp()))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }

        if (label.equalsIgnoreCase("slock"))
        {
            if (args.length < 1)
            {
                sender.sendMessage(ChatColor.RED + "Usage: /slock <username>");
                return true;
            }

            String target = args[0].toLowerCase();
            File slock = new File(Bridgecord.getInstance().getDataFolder(), target + ".slock");
            if (slock.exists())
            {
                if (!slock.delete()) slock.deleteOnExit();
                sender.sendMessage(ChatColor.RED + "Staff Lock has been removed for: " + target);
            } else {
                createLock(slock);
                sender.sendMessage(ChatColor.GREEN + "Staff Lock has been created for: " + target);
            }
        }
        return true;
    }

    private void createLock(File file)
    {
        try
        {
            file.createNewFile();
        } catch (Exception ignored) {}
    }
}
