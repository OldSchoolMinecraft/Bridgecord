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
    private final HashMap<String, LinkData> linkRequests = new HashMap<>();
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

    public String startLinkProcess(String username, String discordID)
    {
        String code = generateCode();
        linkRequests.put(username, new LinkData(discordID, username, code, System.currentTimeMillis()));
        return code;
    }

    public boolean completeLinkProcess(String username, String code)
    {
        if (!linkRequests.containsKey(username)) return false;
        LinkData req = linkRequests.get(username);
        if (req.linkCode.equals(code))
        {
            linkRequests.remove(username);
            dataSource.linkDiscordAccount(username, req.discordID);
            return true;
        }
        return false;
    }

    public AbstractDataSource getDataSource()
    {
        return dataSource;
    }

    private String generateCode()
    {
        return String.format("%06d", random.nextInt(999999));
    }
}
