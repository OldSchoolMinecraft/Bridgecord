package net.oldschoolminecraft.bcord.data;

import net.oldschoolminecraft.bcord.util.LinkData;
import net.oldschoolminecraft.bcord.util.MySQLConnectionPool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RemoteDataSource extends AbstractDataSource
{
    private MySQLConnectionPool connectionPool;

    public RemoteDataSource(MySQLConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    @Override
    protected LinkData loadDiscordLinkData(String username)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("SELECT * FROM link_data WHERE username = ?"))
        {
            stmt.setString(1, username);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            if (rs.next())
                return new LinkData(rs.getString("discordID"), rs.getString("username"), rs.getLong("linkTime"));
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public LinkData loadDiscordLinkDataByID(String discordID)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("SELECT * FROM link_data WHERE discordID = ?"))
        {
            stmt.setString(1, discordID);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            if (rs.next())
                return new LinkData(rs.getString("discordID"), rs.getString("username"), rs.getLong("linkTime"));
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void linkDiscordAccount(String username, String discordID)
    {
        try (PreparedStatement stmt = connectionPool.getConnection().prepareStatement("INSERT INTO link_data (username, discordID, linkTime) VALUES (?, ?, ?)"))
        {
            stmt.setString(1, username);
            stmt.setString(2, discordID);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}