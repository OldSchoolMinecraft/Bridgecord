package net.oldschoolminecraft.bcord.data;


import net.oldschoolminecraft.bcord.util.LinkData;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class AbstractDataSource implements DataSource
{
    protected abstract LinkData loadDiscordLinkData(String username);
    public abstract void linkDiscordAccount(String username, String discordID);

    public boolean isDiscordAccountLinked(String username)
    {
        return loadDiscordLinkData(username) != null;
    }

    public LinkData getDiscordLinkData(String username)
    {
        return loadDiscordLinkData(username);
    }
}
