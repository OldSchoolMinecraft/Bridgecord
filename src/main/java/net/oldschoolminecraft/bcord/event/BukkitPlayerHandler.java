package net.oldschoolminecraft.bcord.event;

import com.legacyminecraft.poseidon.event.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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

    public void onEntityDamage(EntityDamageEvent event)
    {
//        super.onEntityDamage(event);
    }

    public void onPlayerDeath(PlayerDeathEvent event)
    {
//        super.onPlayerDeath(event);
    }

    public void onEntityDeath(EntityDeathEvent event)
    {
        super.onEntityDeath(event);
    }
}
