package net.oldschoolminecraft.bcord.event;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitPlayerHandler extends BridgecordHandler
{
    public void onPlayerChat(PlayerChatEvent event)
    {
        super.onPlayerChat(event);
    }

    public void onPlayerJoin(PlayerJoinEvent event)
    {
        super.onPlayerJoin(event);
    }

    public void onPlayerQuit(PlayerQuitEvent event)
    {
        super.onPlayerQuit(event);
    }
}
