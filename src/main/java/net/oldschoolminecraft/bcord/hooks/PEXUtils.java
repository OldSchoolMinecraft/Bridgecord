package net.oldschoolminecraft.bcord.hooks;

import org.bukkit.Bukkit;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.lang.reflect.Field;

public class PEXUtils
{
    private PermissionManager permissionsManager;
    private boolean hooked = false;
    private PermissionsEx pex;

    public PEXUtils()
    {
        try
        {
            pex = (PermissionsEx) Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx");
            if (!isInstalled()) return;
            Field field = pex.getClass().getDeclaredField("permissionsManager");
            field.setAccessible(true);
            permissionsManager = (PermissionManager) field.get(pex);
            hooked = true;
        } catch (NoClassDefFoundError | Exception ex) {
            System.out.println("[Bridgecord] Failed to hook PermissionsEx");
        }
    }

    public boolean isInstalled()
    {
        return pex != null;
    }

    public String getFirstGroup(String username)
    {
        if (!hooked) return null;
        return permissionsManager.getUser(username).getGroupsNames()[0];
    }

    public String getWholePrefix(String username)
    {
        if (!hooked) return null;
        return permissionsManager.getUser(username).getPrefix();
    }
}
