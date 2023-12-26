package net.oldschoolminecraft.bcord.util;

import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.data.AbstractDataSource;
import net.oldschoolminecraft.bcord.data.RemoteDataSource;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Random;

public class DiscordLinkHandler
{
    private final Bridgecord plugin;
    private final HashMap<String, String> linkRequests = new HashMap<>();
    private final Random random = new Random();
    private AbstractDataSource dataSource;
    private boolean sql;

    public DiscordLinkHandler(Bridgecord plugin, AbstractDataSource dataSource)
    {
        this.plugin = plugin;
        this.dataSource = dataSource;

        sql = (dataSource instanceof RemoteDataSource);
    }

    public boolean isAccountLinked(String username)
    {
        return dataSource.isDiscordAccountLinked(username);
    }

    public LinkData loadLinkDataByID(String discordID)
    {
        if (!sql) throw new NotImplementedException("Bridgecord does not currently support local data sources. Sorry!");

        RemoteDataSource dataSource = (RemoteDataSource) this.dataSource;

        return dataSource.loadDiscordLinkDataByID(discordID);
    }

    public LinkData loadLinkData(String username)
    {
        return dataSource.getDiscordLinkData(username);
    }

    public String startLinkProcess(String username)
    {
        String code = generateCode();
        linkRequests.put(username, code);
        return code;
    }

    public boolean completeLinkProcess(String username, String code)
    {
        boolean complete = linkRequests.containsKey(username) && linkRequests.get(username).equals(code);
        if (complete)
        {
            linkRequests.remove(username);
            dataSource.linkDiscordAccount(username, code);
        }
        return complete;
    }

    private String generateCode()
    {
        return String.format("%06d", random.nextInt(999999));
    }
}
