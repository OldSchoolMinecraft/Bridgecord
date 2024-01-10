package net.oldschoolminecraft.bcord.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;

public class EssUtils
{
    private final Essentials essentials;

    public EssUtils()
    {
        essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
    }

    public User getUser(String username)
    {
        return essentials.getOfflineUser(username);
    }

    public boolean isInstalled()
    {
        return essentials != null;
    }
}
