package net.oldschoolminecraft.bcord.cmd.bot;

import net.oldschoolminecraft.bcord.util.PluginConfig;

public class BotCommandConfig
{
    private boolean enabled;
    private final String label;
    private boolean reply;
    private boolean primaryServerOnly;

    public BotCommandConfig(PluginConfig config, String key)
    {
        this(config, key, true, true);
    }

    public BotCommandConfig(PluginConfig config, String key, boolean reply, boolean primaryServerOnly)
    {
        enabled = (boolean) config.getConfigOption("commands." + key + ".enabled", true);
        label = String.valueOf(config.getConfigOption("commands." + key + ".label", key));
        this.reply = (boolean) config.getConfigOption("commands." + key + ".reply", reply);
        this.primaryServerOnly = (boolean) config.getConfigOption("commands." + key + ".primaryServerOnly", primaryServerOnly);
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

    public boolean isPrimaryServerOnly()
    {
        return primaryServerOnly;
    }

    public void setPrimaryServerOnly(boolean flag)
    {
        primaryServerOnly = flag;
    }

    public void setEnabled(boolean flag)
    {
        enabled = flag;
    }

    public void setShouldReply(boolean flag)
    {
        reply = flag;
    }
}
