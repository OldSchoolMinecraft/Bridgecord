package net.oldschoolminecraft.bcord.cmd;

import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.event.BridgecordHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

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
                Bridgecord.getInstance().getBotListener().reloadCommands();
                sender.sendMessage(ChatColor.GREEN + "Bridgecord configuration & event handlers reloaded");
                return true;
            }

            if (args.length >= 1 && args[0].equalsIgnoreCase("debug"))
            {
                BridgecordHandler currentHandler = Bridgecord.getInstance().getCurrentEventHandler();
                boolean disabled = currentHandler.isDisabled();
                boolean useSuperEvents = Bridgecord.getInstance().getConfig().getBoolean("priority.useSuperEvents", false);
                Event.Priority eventPriority = Event.Priority.valueOf(Bridgecord.getInstance().getConfig().getString("priority.eventPriority", "Highest"));
                String hackyRegex = String.valueOf(Bridgecord.getInstance().getConfig().getConfigOption("hackyRegexFix"));
                boolean hackyRegexFix = (hackyRegex != null);

                System.out.println("Current handler enabled? " + (!disabled ? "True" : "False"));
                System.out.println("Event handler priority: " + eventPriority);
                System.out.println("Using super events? " + (useSuperEvents ? "True" : "False"));
                System.out.println("Hacky regex fix? " + (hackyRegexFix ? "True" + " (" + hackyRegex + ")" : "False"));

                return true;
            }

            if (args.length >= 1 && stringMatches(args[0], "enable", "disable"))
            {
                boolean disable = args[0].equalsIgnoreCase("disable");
                Bridgecord.getInstance().getCurrentEventHandler().setDisabled(disable);
                if (disable) sender.sendMessage(ChatColor.RED + "Bridgecord event handlers are now disabled!");
                else sender.sendMessage(ChatColor.GREEN + "Bridgecord event handlers are now enabled!");
            }

            sender.sendMessage(ChatColor.GRAY + "Bridgecord v" + Bridgecord.getInstance().getDescription().getVersion() + " by " + ChatColor.RED + "moderator_man");
        }

        return true;
    }

    private boolean stringMatches(String input, String... matches)
    {
        for (String match : matches) return input.equalsIgnoreCase(match);
        return false;
    }
}
