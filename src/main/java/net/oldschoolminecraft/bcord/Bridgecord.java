package net.oldschoolminecraft.bcord;

import com.johnymuffin.discordcore.DiscordBot;
import com.johnymuffin.discordcore.DiscordCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Bridgecord extends JavaPlugin
{
    private static Bridgecord instance;
    public static Bridgecord getInstance()
    {
        return instance;
    }

    private PluginConfig config;
    private DiscordBot bot;

    public void onEnable()
    {
        instance = this;
        config = new PluginConfig(new File(getDataFolder(), "config.yml"));
        config.reload();
        DiscordCore core = (DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore");
        bot = core.getDiscordBot();

        bot.jda.addEventListener(new BotListener());
        getServer().getPluginManager().registerEvents(new PlayerHandler(this), this);

        System.out.println("Bridgecord enabled");
    }

    public DiscordBot getBot()
    {
        return bot;
    }

    public PluginConfig getConfig()
    {
        return config;
    }

    public void onDisable()
    {
        System.out.println("Bridgecord disabled");
    }
}
