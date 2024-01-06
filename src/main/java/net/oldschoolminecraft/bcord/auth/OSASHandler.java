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
        if (!osas.fallbackManager.isRegistered(username.toLowerCase())) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        osas.fallbackManager.authenticatePlayer(username.toLowerCase());
        osas.fallbackManager.unfreezePlayer(username.toLowerCase());
    }

    public void deleteAccount(String username) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("OSAS is not installed");
        osas.fallbackManager.deleteAccount(username.toLowerCase());
    }

    @Override
    public void updatePassword(String username, String newPassword) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("OSAS is not installed");
        if (!osas.fallbackManager.isRegistered(username.toLowerCase())) throw new AuthHandlerException("Player is not registered");
        Account account = osas.fallbackManager.getAccount(username.toLowerCase());
        String[] updatedPwdHash = Util.hash(newPassword);
        account.password = updatedPwdHash[0];
        account.salt = updatedPwdHash[1];
        account.approved = false;
        osas.fallbackManager.updateAccount(account);
        if (Bukkit.getOfflinePlayer(username).isOnline())
        {
            osas.fallbackManager.deauthenticatePlayer(username.toLowerCase());
            osas.fallbackManager.freezePlayer(username.toLowerCase());
        }
    }

    public JavaPlugin getPlugin() throws AuthHandlerException
    {
        return osas;
    }

    public boolean isAuthorized(String username)
    {
        return osas.fallbackManager.isAuthenticated(username.toLowerCase());
    }

    public boolean isInstalled()
    {
        return osas != null;
    }
}
