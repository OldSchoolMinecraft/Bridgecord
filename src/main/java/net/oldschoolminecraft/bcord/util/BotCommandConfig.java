package net.oldschoolminecraft.bcord.util;

public class BotCommandConfig
{
    private PluginConfig config;
    private boolean enabled;
    private String label;

    public BotCommandConfig(PluginConfig config, String key)
    {
        this.config = config;
        enabled = (boolean) config.getConfigOption("commands." + key + ".enabled");
        label = String.valueOf(config.getConfigOption("commands." + key + ".label"));
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getLabel()
    {
        return label;
    }
}
