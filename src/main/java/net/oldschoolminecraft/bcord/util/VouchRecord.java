package net.oldschoolminecraft.bcord.util;

public class VouchRecord
{
    public String username;
    public long timestamp;

    public VouchRecord() {}

    public VouchRecord(String username)
    {
        this.username = username;
        this.timestamp = System.currentTimeMillis();
    }
}
