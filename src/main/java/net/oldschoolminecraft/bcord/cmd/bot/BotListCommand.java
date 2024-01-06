package net.oldschoolminecraft.bcord.cmd.bot;

import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BotListCommand extends BotCommand
{
    public BotListCommand()
    {
        super("list");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        if (Bukkit.getOnlinePlayers().length == 0)
        {
            respond(event.getMessage(), String.valueOf(config.getConfigOption("serverEmptyMessage")), getConfig().shouldReply());
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
            respond(event.getMessage(), String.valueOf(config.getConfigOption("serverEmptyMessage")), getConfig().shouldReply());
            return;
        }
        String pre = sb.toString().trim();
        respond(event.getMessage(), "Online players (" + Math.max(Bukkit.getOnlinePlayers().length - invisSub, 0) + "):\n`" + pre.substring(0, pre.length() - 1) + "`", getConfig().shouldReply());
    }
}
