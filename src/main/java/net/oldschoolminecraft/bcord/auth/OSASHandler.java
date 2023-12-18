package net.oldschoolminecraft.bcord.auth;

import com.oldschoolminecraft.osas.OSAS;
import com.oldschoolminecraft.osas.Util;
import com.oldschoolminecraft.osas.impl.fallback.Account;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OSASHandler implements AuthPluginHandler
{
    private OSAS osas;

    public OSASHandler()
    {
        try
        {
            osas = (OSAS) Bukkit.getPluginManager().getPlugin("OSAS");
        } catch (Exception ignored) {} // not installed
    }

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("OSAS is not installed");
        if (!osas.fallbackManager.isRegistered(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        osas.fallbackManager.authenticatePlayer(username);
        osas.fallbackManager.unfreezePlayer(username);
    }

    public void deleteAccount(String username) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("OSAS is not installed");
        osas.fallbackManager.deleteAccount(username);
    }

    @Override
    public void updatePassword(String username, String newPassword) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("OSAS is not installed");
        if (!osas.fallbackManager.isRegistered(username)) throw new AuthHandlerException("Player is not registered");
        Account account = osas.fallbackManager.getAccount(username);
        String[] updatedPwdHash = Util.hash(newPassword);
        account.password = updatedPwdHash[0];
        account.salt = updatedPwdHash[1];
        osas.fallbackManager.updateAccount(account);
    }

    public JavaPlugin getPlugin() throws AuthHandlerException
    {
        return osas;
    }

    public boolean isAuthorized(String username)
    {
        return osas.fallbackManager.isAuthenticated(username);
    }

    public boolean isInstalled()
    {
        return osas != null;
    }
}
