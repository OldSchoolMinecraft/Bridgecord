package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StaffLockHandler
{
    private static final StaffLockHandler instance = new StaffLockHandler();

    public static StaffLockHandler getInstance()
    {
        return instance;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final ConcurrentHashMap<String, Long> unlocked = new ConcurrentHashMap<>();

    public void unlock(String username)
    {
        String lowerUsername = username.toLowerCase();
        unlocked.put(lowerUsername, System.currentTimeMillis());
        scheduleRelock(lowerUsername);
    }

    private void scheduleRelock(String username)
    {
        scheduler.schedule(() ->
        {
            // If the player is still online, check again in 1 minute
            if (Bukkit.getPlayer(username) != null)
            {
                scheduleRelock(username);
                return;
            }

            // Remove unlock status only if 5 minutes have passed
            Long unlockTime = unlocked.get(username);
            if (unlockTime != null && System.currentTimeMillis() - unlockTime >= TimeUnit.MINUTES.toMillis(5))
                unlocked.remove(username);
        }, 1, TimeUnit.MINUTES); // Check every minute for relocking
    }

    public boolean isUnlocked(String username)
    {
        return unlocked.containsKey(username.toLowerCase());
    }

    public boolean hasLock(String username)
    {
        return new File(Bridgecord.getInstance().getDataFolder(), username.toLowerCase() + ".slock").exists();
    }
}
