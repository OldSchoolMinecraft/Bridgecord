package net.oldschoolminecraft.bcord.auth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;

import java.lang.reflect.Field;

public class AuthMeHandler implements AuthPluginHandler
{
    private AuthMe authMe;
    private DataSource database;

    public AuthMeHandler()
    {
        try
        {
            authMe = (AuthMe) Bukkit.getPluginManager().getPlugin("AuthMe");
            if (authMe == null) return; // not installed, don't attempt setup
            Field field = authMe.getClass().getDeclaredField("database");
            field.setAccessible(true);
            database = (DataSource) field.get(authMe);
        } catch (Exception ex) {
            System.out.println("[Bridgecord] Failed to access AuthMe database (not installed?)");
        }
    }

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
        if (!database.isAuthAvailable(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        PlayerCache.getInstance().addPlayer(new PlayerAuth(username, "Bridgecord", ip));
    }

    public void deleteAccount(String username) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
        PlayerCache.getInstance().removePlayer(username);
        database.removeAuth(username);
    }

    public JavaPlugin getPlugin() throws AuthHandlerException
    {
        return authMe;
    }

    public boolean isAuthorized(String username)
    {
        return PlayerCache.getInstance().isAuthenticated(username);
    }

    public boolean isInstalled()
    {
        return authMe != null;
    }
}