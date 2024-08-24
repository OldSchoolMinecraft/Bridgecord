package net.oldschoolminecraft.bcord.hooks;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;

public class OSMPLUtils
{
    private static final Gson gson = new Gson();

    public OSMPLUser getUserData(String username)
    {
        try (FileReader reader = new FileReader("playerdata/" + username.toLowerCase() + ".json"))
        {
            return gson.fromJson(reader, OSMPLUser.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class OSMPLUser
    {
        public String name;
        public String ip;
        public long playTime;
        public long firstJoin;
        public int kills;
        public int deaths;
        public Punishment currentBan;
        public Punishment currentMute;
    }

    public static class Punishment
    {
        public String reason;
        public long time;
        public long expire;
        public PunishmentType type;
    }

    public enum PunishmentType
    {
        BAN, MUTE, WARN
    }
}
