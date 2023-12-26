package net.oldschoolminecraft.bcord.util;

public class LinkData
{
    public String discordID;
    public String username;
    public String linkCode;
    public long linkTime;

    public LinkData() {}

    public LinkData(String discordID, String username, String linkCode, long linkTime)
    {
        this.discordID = discordID;
        this.username = username;
        this.linkCode = linkCode;
        this.linkTime = linkTime;
    }
}
