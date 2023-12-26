package net.oldschoolminecraft.bcord.auth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Settings;

import java.lang.reflect.Field;

public class AuthMeHandler implements AuthPluginHandler
{
    private AuthMe authMe;
    private DataSource database;
    private Settings settings;

    public AuthMeHandler()
    {
        try
        {
            authMe = (AuthMe) Bukkit.getPluginManager().getPlugin("AuthMe");
            if (authMe == null) return; // not installed, don't attempt setup

            // make database accessible & get a handle
            database = getDatabase();

            if (database == null)
            {
                Bukkit.getPluginManager().enablePlugin(authMe);
                database = getDatabase();
                if (database == null)
                    throw new AuthHandlerException("Failed to initialize AuthMe module as the database is uninitialized or irretrievable");
            }

            // make pws accessible & get a handle
            settings = getSettings();

            if (settings == null)
            {
                Bukkit.getPluginManager().enablePlugin(authMe);
                settings = getSettings();
                if (settings == null)
                    throw new AuthHandlerException("Failed to initialize AuthMe module as the settings are uninitialized or irretrievable");
            }
        } catch (Exception ex) {
            System.out.println("[Bridgecord] Reflection failed on AuthMe (not installed?)");
            ex.printStackTrace(System.err);
        }
    }

    private DataSource getDatabase() throws NoSuchFieldException, IllegalAccessException
    {
        Field field = authMe.getClass().getDeclaredField("database");
        field.setAccessible(true);
        return (DataSource) field.get(authMe);
    }

    private Settings getSettings() throws NoSuchFieldException, IllegalAccessException
    {
        Field field = authMe.getClass().getDeclaredField("settings");
        field.setAccessible(true);
        return (Settings) field.get(authMe);
    }

    public void authenticate(String username, String ip) throws AuthHandlerException
    {
        if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
        if (!database.isAuthAvailable(username)) throw new AuthHandlerException("User account is not registered"); // prevent unregistered users from bypassing auth
        PlayerCache.getInstance().addPlayer(new PlayerAuth(username, "Bridgecord", ip, System.currentTimeMillis()));
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
        try
        {
            if (!isInstalled()) throw new AuthHandlerException("AuthMe is not installed");
            if (PlayerCache.getInstance().getAuth(username) == null) throw new AuthHandlerException("Player is not registered");
            PlayerAuth auth = PlayerCache.getInstance().getAuth(username);
            auth.setHash(PasswordSecurity.getHash(settings.getPasswordHash(), newPassword));
            database.updatePassword(auth);
        } catch (Exception ex) {
            throw new AuthHandlerException(ex.getMessage());
        }
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
