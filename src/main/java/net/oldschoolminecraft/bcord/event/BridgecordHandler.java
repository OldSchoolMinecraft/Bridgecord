package net.oldschoolminecraft.bcord.event;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserData;
import com.johnymuffin.discordcore.DiscordBot;
import com.oldschoolminecraft.jp.JoinsPlus;
import com.oldschoolminecraft.jp.Message;
import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.entities.TextChannel;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.hooks.EssUtils;
import net.oldschoolminecraft.bcord.hooks.OSMPLUtils;
import net.oldschoolminecraft.bcord.util.PluginConfig;
import net.oldschoolminecraft.bcord.util.StaffLockHandler;
import net.oldschoolminecraft.bcord.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.File;
import java.util.*;

public abstract class BridgecordHandler extends PlayerListener
{
    private static final Bridgecord plugin = Bridgecord.getInstance();
    private static final DiscordBot bot = Bridgecord.getInstance().getBot();
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();
    private static List<String> blockedKeywords = new ArrayList<>();
    private boolean DISABLED = false;

    public BridgecordHandler()
    {
        blockedKeywords = (ArrayList<String>) config.getConfigOption("blockedKeywords");
    }

    public void onPlayerChat(PlayerChatEvent event)
    {
        if (DISABLED || event.isCancelled()) return;

        boolean preventUnauthorizedChats = (boolean) config.getConfigOption("preventUnauthorizedChats");
        if (preventUnauthorizedChats)
        {
            AuthPluginHandler authHandler = Util.selectAuthPlugin();
            if (!authHandler.isInstalled()) return;
            if (!authHandler.isAuthorized(event.getPlayer().getName())) return;
        }

        if (plugin.getEssUtils().isInstalled() && plugin.getEssUtils().getUser(event.getPlayer().getName()).isMuted())
            return; // nope.avi

        OSMPLUtils.OSMPLUser osmplUser = plugin.getOSMPLUtils().getUserData(event.getPlayer().getName());
        if (osmplUser != null && osmplUser.currentMute != null)
            return; // nope.avi

        for (String keyword : blockedKeywords)
        {
            if (event.getMessage().contains(keyword))
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Your message contains a blocked keyword!");
                return;
            }
        }

        String hackyRegex = config.getString("hackyRegexFix");
        boolean hackyRegexFix = (hackyRegex != null);

        if (hackyRegexFix)
        {
            long numColons = countOccurrences(event.getMessage(), ':');
            String pre = numColons > 1 ? event.getMessage().replaceFirst(":", "") : event.getMessage();
            String message = pre.replaceAll(hackyRegex, "");
            event.setMessage(message);
        }

        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            String formattedMessage = Util.processMessage(String.valueOf(config.getConfigOption("bridgeMessageFormat.shownInDiscord")), new HashMap<String, String>()
            {{
                put("{name}", Util.stripAllColor(event.getPlayer().getName()));
                put("{displayName}", Util.stripAllColor(event.getPlayer().getDisplayName()));
                put("{msg}", Util.stripAllColor(event.getMessage()));
                if (config.getBoolean("usePEXPrefixes", false))
                {
                    put("{group}", Util.stripAllColor(plugin.getPexUtils().getFirstGroup(event.getPlayer().getName())));
                    put("{prefix}", Util.stripAllColor(plugin.getPexUtils().getWholePrefix(event.getPlayer().getName())));
                }
            }});
            deliverMessage(formattedMessage);
        }, 0L);
    }

    public void onPlayerPreLogin(PlayerPreLoginEvent event)
    {
        if (DISABLED) return;

        File slock = new File(Bridgecord.getInstance().getDataFolder(), event.getName().toLowerCase() + ".slock");
        if (slock.exists())
        {
            if (!StaffLockHandler.getInstance().isUnlocked(event.getName()))
            {
                event.cancelPlayerLogin(ChatColor.RED + "This account is currently locked.");
                return;
            }
            event.allow();
        }
    }

    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (DISABLED) return;

        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            JoinsPlus jp = (JoinsPlus) Bukkit.getServer().getPluginManager().getPlugin("JoinsPlus");
            Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
            boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
            boolean invisimanInstalled = invisiman != null;
            boolean statsOnJoinLeave = (boolean) config.getConfigOption("bridgeMessageFormat.statsOnJoinLeave");
            String statsFormat = String.valueOf(config.getConfigOption("bridgeMessageFormat.statsFormat"));
            String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
            String msg = "*__" + event.getPlayer().getName() + "__ has connected*";

            if (jp != null)
            {
                Message jpMessage = jp.loadMessage(event.getPlayer().getName());
                if (jpMessage != null)
                {
                    msg = "*" + jpMessage.join + "*";
                    msg = msg.replace("%player%", "__" + event.getPlayer().getName() + "__");
                }
            }
            if (invisimanInstalled && useInvisiman && invisiman.isVanished(event.getPlayer())) return;
            if (event.getPlayer().hasPermission(hideWithPerm)) return;

            if (statsOnJoinLeave)
            {
                msg += " " + Util.processMessage(statsFormat, new HashMap<String, String>()
                {{
                    put("{online}", String.valueOf(Bukkit.getOnlinePlayers().length));
                    put("{maxPlayers}", String.valueOf(Bukkit.getMaxPlayers()));
                }});
            }

            deliverMessage(Util.stripAllColor(msg));
        }, 0L);

        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            if (plugin.getEssUtils().isInstalled())
            {
                User user = plugin.getEssUtils().getUser(event.getPlayer().getName());
                boolean isGodOn = user.isGodModeEnabled();
                boolean isAllowed = event.getPlayer().hasPermission("essentials.god") || event.getPlayer().isOp();
                boolean finalFlag = (isGodOn && isAllowed);
                user.setGodModeEnabled(finalFlag);
                if (config.getBoolean("dev.debug", false)) event.getPlayer().sendMessage(ChatColor.RED + "God mode: " + (finalFlag ? (ChatColor.GREEN + "ON") : "OFF"));
            }
        });
    }
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (DISABLED) return;

        scheduler.scheduleAsyncDelayedTask(plugin, () ->
        {
            JoinsPlus jp = (JoinsPlus) Bukkit.getServer().getPluginManager().getPlugin("JoinsPlus");
            Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
            boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
            boolean invisimanInstalled = invisiman != null;
            boolean statsOnJoinLeave = (boolean) config.getConfigOption("bridgeMessageFormat.statsOnJoinLeave");
            String statsFormat = String.valueOf(config.getConfigOption("bridgeMessageFormat.statsFormat"));
            String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
            String msg = "*__" + event.getPlayer().getName() + "__ has disconnected*";
            if (jp != null)
            {
                Message jpMessage = jp.loadMessage(event.getPlayer().getName());
                if (jpMessage != null)
                {
                    msg = "*" + jpMessage.quit + "*";
                    msg = msg.replace("%player%", "__" + event.getPlayer().getName() + "__");
                }
            }
            if (invisimanInstalled && useInvisiman && invisiman.isVanished(event.getPlayer())) return;
            if (event.getPlayer().hasPermission(hideWithPerm)) return;

            if (statsOnJoinLeave)
            {
                msg += " " + Util.processMessage(statsFormat, new HashMap<String, String>()
                {{
                    put("{online}", String.valueOf(Bukkit.getOnlinePlayers().length - (event.getPlayer().isOnline() ? 1 : 0)));
                    put("{maxPlayers}", String.valueOf(Bukkit.getMaxPlayers()));
                }});
            }

            deliverMessage(Util.stripAllColor(msg));
        }, 0L);
    }

    public void setDisabled(boolean flag)
    {
        DISABLED = flag;
    }

    public void disable()
    {
        DISABLED = true;
    }

    public boolean isDisabled()
    {
        return DISABLED;
    }

    private long countOccurrences(String str, char target)
    {
        return str.chars().filter(ch -> ch == target).count();
    }

    private void deliverMessage(String message)
    {
        List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());
        for (String channelID : channelIDs)
        {
            TextChannel channel = Bridgecord.getInstance().getBot().jda.getTextChannelById(channelID);
            if (channel == null)
            {
                System.out.println("[Bridgecord] Failed to get JDA handle for text channel: " + channelID);
                continue;
            }
            Objects.requireNonNull(channel).sendMessage(message).queue();
        }
    }
}
