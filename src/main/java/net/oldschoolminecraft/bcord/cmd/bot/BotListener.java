package net.oldschoolminecraft.bcord.cmd.bot;

import com.oldschoolminecraft.vanish.Invisiman;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.application.GenericApplicationCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.cmd.StaffLockCommand;
import net.oldschoolminecraft.bcord.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotListener extends ListenerAdapter
{
    private static final PluginConfig config = Bridgecord.getInstance().getConfig();
    private final String cmdPrefix = String.valueOf(config.getConfigOption("commands.prefix"));
    private final ArrayList<BotCommand> botCommands = new ArrayList<>();

    public BotListener()
    {
        reloadCommands();
    }

    public void reloadCommands()
    {
        boolean linkingFeaturesEnabled = config.getBoolean("discordLinking.enabled", true);

        botCommands.clear();
        botCommands.add(new BotListCommand());
        if (linkingFeaturesEnabled)
        {
            botCommands.add(new BotLinkCommand());
            botCommands.add(new BotResetCommand());
            botCommands.add(new BotAuthCommand());
            botCommands.add(new BotStaffLockCommand());
            botCommands.add(new BotVouchCommand());
            botCommands.add(new BotWhitelistCommand());
            // botCommands.add(new BotMFACommand());
        }
    }

    private String getPlayerList()
    {
        String hideWithPerm = String.valueOf(config.getConfigOption("hidePlayersWithPermission"));
        Invisiman invisiman = (Invisiman) Bukkit.getServer().getPluginManager().getPlugin("Invisiman");
        boolean useInvisiman = (boolean) config.getConfigOption("useInvisiman");
        boolean invisimanInstalled = invisiman != null;
        StringBuilder sb = new StringBuilder();
        int invisSub = 0;
        for (Player p : Bukkit.getOnlinePlayers())
        {
            boolean playerIsVanished = (!useInvisiman && p.hasPermission(hideWithPerm)) || (useInvisiman && invisimanInstalled && invisiman.isVanished(p));
            if (!playerIsVanished) sb.append(p.getName()).append(", ");
            else invisSub++;
        }
        String pre = sb.toString().trim();
        return pre.substring(0, pre.length() - 1);
    }

    public void onGenericApplicationCommand(@NotNull GenericApplicationCommandEvent event)
    {
        if (event.getCommand().getName().equalsIgnoreCase("list"))
        {
            MessageEmbed eb = new EmbedBuilder()
                    .setTitle("Player List")
                    .setDescription("`" + getPlayerList() + "`")
                    .setFooter("Bridgecord v" + Bridgecord.getInstance().getDescription().getVersion() + " (Beta)")
                    .build();
        }
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().isSystem()) return;
        if (event.getMessage().isWebhookMessage()) return;

        String strippedMsg = event.getMessage().getContentStripped();
        if (strippedMsg.startsWith(cmdPrefix))
        {
            boolean hasArgs = strippedMsg.contains(" ");
            String[] parts = hasArgs ? strippedMsg.split(" ") : new String[] { strippedMsg };
            String guildID = event.getGuild().getId();
            for (BotCommand cmd : botCommands)
            {
                boolean enabled = cmd.getConfig().isEnabled();
                boolean primaryServerOnly = cmd.getConfig().isPrimaryServerOnly();
                boolean isFromPrimaryServer = guildID.equals(config.getString("primaryServerID"));
                String labelMatch = cmdPrefix + cmd.getConfig().getLabel();
                String label = parts[0];

                if (label.equalsIgnoreCase(labelMatch))
                {
                    if (!enabled)
                    {
                        System.out.println("[Bridgecord] Discord user attempted to execute a disabled command: " + strippedMsg);
                        continue;
                    }

                    if (primaryServerOnly && !isFromPrimaryServer)
                    {
                        System.out.println("[Bridgecord] Discord user attempted to execute a primary-only command from a non-primary server: " + strippedMsg);
                        continue;
                    }

                    System.out.println("[Bridgecord] Discord user executed bot command: " + strippedMsg);
                    asyncImmediately(() -> cmd.execute(event));
                    return;
                }
            }

            System.out.println("[Bridgecord] Discord user attempted unknown bot command: " + strippedMsg);
        }

        List<String> channelIDs = config.getStringList("bridgeChannelIDs", Collections.emptyList());
        // read bridge messages from all configured channel IDs
        if (channelIDs.contains(event.getChannel().getId())) //if (event.getChannel().getId().equals(String.valueOf(config.getConfigOption("bridgeChannelID"))))
        {
            asyncImmediately(() ->
            {
                ArrayList<String> msgChunks = new ArrayList<>();

                //TODO: check if user has custom display name, and process accordingly -- check color role config & validate

                String pre = Util.processMessage(translateAlternateColorCodes('&', String.valueOf(config.getConfigOption("bridgeMessageFormat.shownInGame"))), new HashMap<String, String>()
                {{
                    put("{name}", event.getAuthor().getName());
                    put("{displayName}", event.getAuthor().getName());
                    put("{msg}", Util.stripAllColor(event.getMessage().getContentStripped()));
                }});

                MessageReference msgRef = event.getMessage().getMessageReference();
                boolean addReply = config.getBoolean("replies.showInGame", true) && msgRef != null && msgRef.getMessage() != null;
                String replyMsgRaw = addReply ? msgRef.getMessage().getContentRaw() : "";
                String replyMsg = replyMsgRaw;

                if (config.getBoolean("replies.process", false))
                {
                    Pattern pattern = Pattern.compile(config.getString("replies.processRegex", "^\\[[^\\]]+\\]\\s+([a-zA-Z0-9_]+):\\s*(.*)"));
                    Matcher matcher = pattern.matcher(replyMsgRaw);
                    replyMsg = Util.processMessage(translateAlternateColorCodes('&', String.valueOf(config.getConfigOption("bridgeMessageFormat.shownInGame"))), new HashMap<String, String>()
                    {{
                        put("{name}", event.getAuthor().getName());
                        put("&a> {msg}", Util.stripAllColor(matcher.group(2)));
                    }});
                }

                if (pre.length() <= 128)
                    msgChunks.add(pre);
                else {
                    msgChunks.addAll(Util.splitString(pre, 128));
                }

                for (Player p : Bukkit.getOnlinePlayers())
                {
                    if (addReply) p.sendMessage(replyMsg);
                    msgChunks.forEach(p::sendMessage);
                }
            });
        }
    }

    private String translateAlternateColorCodes(char altColorChar, String textToTranslate)
    {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public void respond(Message dcMsg, String replyMsg, boolean reply)
    {
        if (reply)
            dcMsg.reply(replyMsg).queue();
        else dcMsg.getChannel().sendMessage(replyMsg).queue();
    }

    private void asyncImmediately(Runnable runnable)
    {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Bridgecord.getInstance(), runnable, 0L);
    }
}
