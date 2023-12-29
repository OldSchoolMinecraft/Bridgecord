package net.oldschoolminecraft.bcord.util;

public class BotCommandConfig
{
    private final boolean enabled;
    private final String label;
    private final boolean reply;

    public BotCommandConfig(PluginConfig config, String key)
    {
        this(config, key, true);
    }

    public BotCommandConfig(PluginConfig config, String key, boolean reply)
    {
        enabled = (boolean) config.getConfigOption("commands." + key + ".enabled", true);
        label = String.valueOf(config.getConfigOption("commands." + key + ".label", key));
        this.reply = (boolean) config.getConfigOption("commands." + key + ".reply", reply);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean shouldReply()
    {
        return reply;
    }
}
