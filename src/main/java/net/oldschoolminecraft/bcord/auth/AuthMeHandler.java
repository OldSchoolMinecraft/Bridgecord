package net.oldschoolminecraft.bcord.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;

import java.lang.reflect.Field;

public class AuthMeHandler implements AuthPluginHandler
{
    private AuthMe authMe;
    private DataSource database;
    private PasswordSecurity pws;

    public AuthMeHandler()
    {
        try
        {
            authMe = (AuthMe) Bukkit.getPluginManager().getPlugin("AuthMe");
            if (authMe == null) return; // not installed, don't attempt setup

            // make database accessible & get a handle
            Field field = authMe.getClass().getDeclaredField("database");
            field.setAccessible(true);
            database = (DataSource) field.get(authMe);

            // make pws accessible & get a handle
            field = authMe.getClass().getDeclaredField("pws");
            field.setAccessible(true);
            pws = (PasswordSecurity) field.get(authMe);
        } catch (Exception ex) {
            System.out.println("[Bridgecord] Reflection failed on AuthMe (not installed?)");
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

    @Override
    public void updatePassword(String username, String newPassword) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
        if (PlayerCache.getInstance().getAuth(username) == null) throw new AuthHandlerException("Player is not registered");
        PlayerAuth auth = PlayerCache.getInstance().getAuth(username);
        auth.setHash(pws.getHash(newPassword));
        database.updatePassword(auth);
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
