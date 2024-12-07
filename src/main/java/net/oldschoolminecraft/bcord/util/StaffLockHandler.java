package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
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

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ArrayList<String> unlocked = new ArrayList<>();

    public void unlock(String username)
    {
        unlocked.add(username);
        scheduleRelock(username);
    }

    private void scheduleRelock(String username)
    {
        scheduler.schedule(() ->
        {
            // if the player is online, reset the timer without re-locking
            if (Bukkit.getOfflinePlayer(username).isOnline())
            {
                scheduleRelock(username);
                return;
            }
            unlocked.remove(username);
        }, 5, TimeUnit.MINUTES);
    }

    public boolean isUnlocked(String username)
    {
        for (String name : unlocked)
            if (name.equalsIgnoreCase(username))
                return true;
        return false;
    }

    public boolean hasLock(String username)
    {
        return new File(Bridgecord.getInstance().getDataFolder(), username.toLowerCase() + ".slock").exists();
    }
}
