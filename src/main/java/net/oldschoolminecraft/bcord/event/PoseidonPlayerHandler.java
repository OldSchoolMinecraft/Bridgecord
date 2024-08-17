package net.oldschoolminecraft.bcord.event;

import com.legacyminecraft.poseidon.event.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PoseidonPlayerHandler extends BridgecordHandler implements Listener
{
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event)
    {
        super.onPlayerChat(event);
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event)
    {
        super.onPlayerPreLogin(event);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        super.onPlayerJoin(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        super.onPlayerQuit(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        super.onPlayerDeath(event);
    }
}
