package net.oldschoolminecraft.bcord.util;

public class BotCommandConfig
{
    private final boolean enabled;
    private final String label;

    public BotCommandConfig(PluginConfig config, String key)
    {
        enabled = (boolean) config.getConfigOption("commands." + key + ".enabled", true);
        label = String.valueOf(config.getConfigOption("commands." + key + ".label", key));
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
