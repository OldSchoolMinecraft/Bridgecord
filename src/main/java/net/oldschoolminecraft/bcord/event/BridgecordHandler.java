package net.oldschoolminecraft.bcord.event;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserData;
import com.johnymuffin.discordcore.DiscordBot;
import com.legacyminecraft.poseidon.event.PlayerDeathEvent;
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
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;

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

        scheduler.scheduleAsyncDelayedTask(plugin, () -> deliverMessage(formattedMessage), 0L);
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

    private final HashMap<String, Integer> lastHealthMap = new HashMap<>();

    public void onEntityDeath(EntityDeathEvent event)
    {
        if (DISABLED) return;
        if (!config.getBoolean("deathMessagesOnBridge", true)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        String preDeathMessage = player.getName() + " met an unfortunate end!";

        switch (event.getEntity().getLastDamageCause().getCause())
        {
            case FALL:
                preDeathMessage = player.getName() + " fell to their demise!";
                break;
            case LAVA:
                preDeathMessage = player.getName() + " tried to swim in lava!";
                break;
            case DROWNING:
                preDeathMessage = player.getName() + " forgot how to swim!";
                break;
            case CONTACT:
                preDeathMessage = player.getName() + " hugged a cactus too long";
                break;
            case TNT_EXPLOSION:
            case BLOCK_EXPLOSION:
                preDeathMessage = player.getName() + " died in an explosion!";
                break;
            case ENTITY_EXPLOSION:
                preDeathMessage = player.getName() + " was exploded by " + getEntityTypeName(((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager()) + "!";
                break;
            case ENTITY_ATTACK:
            case PROJECTILE:
                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                Entity attacker = damageEvent.getDamager();
                if (attacker instanceof Player)
                {
                    preDeathMessage = player.getName() + " was murdered by " + ((Player) attacker).getName() + "!";
                } else if (attacker instanceof Monster) {
                    if (attacker instanceof Creeper)
                        preDeathMessage = player.getName() + " got friendly with a Creeper!";
                    else preDeathMessage = player.getName() + " was slain by " + getEntityTypeName(attacker) + "!";
                } else if (attacker instanceof Projectile) {
                    preDeathMessage = player.getName() + " was shot by " + getEntityTypeName(attacker) + "!";
                }
                break;
            case SUICIDE:
                preDeathMessage = player.getName() + " took their own life!";
                break;
            case SUFFOCATION:
                preDeathMessage = player.getName() + " suffocated!";
                break;
            case LIGHTNING:
                preDeathMessage = player.getName() + " was struck by lightning!";
                break;
            case FIRE:
            case FIRE_TICK:
                preDeathMessage = player.getName() + " died in a fire!";
                break;
            case VOID:
                preDeathMessage = player.getName() + " was consumed by the void!";
                break;
            case CUSTOM:
            default:
                preDeathMessage = player.getName() + " managed to die inexplicably.";
                break;
        }

        deliverMessage("__*" + preDeathMessage + "*__");
    }

    public String getEntityTypeName(Entity entity)
    {
        if (entity instanceof Monster)
        {
            if (entity instanceof Zombie)
            {
                return "a Zombie";
            } else if (entity instanceof Skeleton) {
                return "a Skeleton";
            } else if (entity instanceof Creeper) {
                return "a Creeper";
            } else if (entity instanceof Spider) {
                return "a Spider";
            }
            return "a monster";
        }
        if (entity instanceof Snowball)
        {
            Snowball snowball = (Snowball) entity;
            if (snowball.getShooter() != null && snowball.getShooter() instanceof CraftPlayer)
                return "a snowball from " + ((CraftPlayer)snowball.getShooter()).getName();
            return "a stray snowball";
        }
        if (entity instanceof Arrow)
        {
            Arrow arrow = (Arrow) entity;
            Entity shooter = arrow.getShooter();
            if (shooter != null)
            {
                if (shooter instanceof Player)
                    return "an arrow from " + ((CraftPlayer) arrow.getShooter()).getName();
                if (shooter instanceof Monster) return getEntityTypeName(shooter);
                return "a stray arrow";
            }
            return "a stray arrow";
        }
        if (entity instanceof Fireball)
        {
            Fireball fireball = (Fireball) entity;
            if (fireball.getShooter() instanceof Ghast)
                return "a Ghast's fireball";
            return "a stray fireball";
        }
        if (entity instanceof CraftPlayer)
            return ((CraftPlayer)entity).getName();
        return entity.getClass().getSimpleName();
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
