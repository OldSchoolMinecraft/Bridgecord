package net.oldschoolminecraft.bcord.data;

import com.google.gson.Gson;
import net.oldschoolminecraft.bcord.util.LinkData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LocalDataSource extends AbstractDataSource
{
    private static final Gson gson = new Gson();

    private File sourceDir;

    public LocalDataSource(File sourceDir)
    {
        this.sourceDir = sourceDir;
    }

    @Override
    protected LinkData loadDiscordLinkData(String username)
    {
        File file = new File(sourceDir, "discord/" + username + ".json");
        file.getParentFile().mkdirs();
        try (FileReader reader = new FileReader(file))
        {
            return gson.fromJson(reader, LinkData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void linkDiscordAccount(String username, String discordID)
    {
        try
        {
            File file = new File(sourceDir, "discord/" + username + ".json");
            file.getParentFile().mkdirs();
            gson.toJson(new LinkData(username, discordID, "N/A", System.currentTimeMillis()), new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
