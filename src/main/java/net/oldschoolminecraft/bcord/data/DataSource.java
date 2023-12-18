package net.oldschoolminecraft.bcord.data;

import net.oldschoolminecraft.bcord.util.LinkData;

public interface DataSource
{
    boolean isDiscordAccountLinked(String username);
    LinkData getDiscordLinkData(String username);
}
