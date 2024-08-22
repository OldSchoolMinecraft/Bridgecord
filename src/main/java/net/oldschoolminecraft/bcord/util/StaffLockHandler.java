package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.Bridgecord;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class StaffLockHandler
{
    private static final StaffLockHandler instance = new StaffLockHandler();

    public static StaffLockHandler getInstance()
    {
        return instance;
    }

    private final ArrayList<String> unlocked = new ArrayList<>();
    private final Timer timer = new Timer();

    public void unlock(String username)
    {
        unlocked.add(username);
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                // if the player is online, reset the timer without re-locking
                if (Bukkit.getOfflinePlayer(username).isOnline())
                {
                    timer.schedule(this, 1000L * 60L * 5L);
                    return;
                }
                unlocked.remove(username);
            }
        }, 1000L * 60L * 5L); // 5 minute authorization TODO: add configuration option
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
