package net.oldschoolminecraft.bcord.auth;

import org.bukkit.plugin.java.JavaPlugin;

public interface AuthPluginHandler
{
    void authenticate(String username, String ip) throws AuthHandlerException;
    void deleteAccount(String username) throws AuthHandlerException;
    JavaPlugin getPlugin() throws AuthHandlerException;
    boolean isAuthorized(String username);
    boolean isInstalled();
}
