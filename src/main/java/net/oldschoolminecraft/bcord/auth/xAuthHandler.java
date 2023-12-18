package net.oldschoolminecraft.bcord.auth;

import com.cypherx.xauth.xAuth;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class xAuthHandler implements AuthPluginHandler
{
    private xAuth xauth;

    public xAuthHandler()
    {
        try
        {
            xauth = (xAuth) Bukkit.getPluginManager().getPlugin("xAuth");
        } catch (Exception ignored) {} // not installed
    }

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("xAuth is not installed");
        if (!xauth.isRegistered(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        xauth.login(Bukkit.getPlayer(username));
    }

    public void deleteAccount(String username) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("xAuth is not installed");
        xauth.removeAuth(username);
        Player target = Bukkit.getPlayer(username);
        if (target != null)
        {
            if (xauth.mustRegister(target))
            {
                xauth.saveLocation(target);
                xauth.saveInventory(target);
            }
        }
    }

    @Override
    public void updatePassword(String username, String newPassword) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("xAuth is not installed");
        if (!xauth.isRegistered(username)) throw new AuthHandlerException("Player is not registered");
        xauth.changePass(username, newPassword);
    }

    public JavaPlugin getPlugin() throws AuthHandlerException
    {
        return xauth;
    }

    public boolean isAuthorized(String username)
    {
        return xauth.isLoggedIn(Bukkit.getPlayer(username));
    }

    public boolean isInstalled()
    {
        return xauth != null;
    }
}
