package net.oldschoolminecraft.bcord.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class StaffLockHandler
{
    private static StaffLockHandler instance;

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
}
