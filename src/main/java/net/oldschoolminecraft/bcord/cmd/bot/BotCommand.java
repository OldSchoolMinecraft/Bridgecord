package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.util.PluginConfig;

public abstract class BotCommand
{
    protected final PluginConfig config = Bridgecord.getInstance().getConfig();
    protected final String cmdPrefix = String.valueOf(config.getConfigOption("commands.prefix"));
    private final BotCommandConfig cmdConfig;

    public BotCommand(String label)
    {
        cmdConfig = new BotCommandConfig(config, label);
    }

    public abstract void execute(MessageReceivedEvent event);

    public void respond(Message dcMsg, String replyMsg, boolean reply)
    {
        if (reply)
            dcMsg.reply(replyMsg).queue();
        else dcMsg.getChannel().sendMessage(replyMsg).queue();
    }

    public void queueDM(User user, String message)
    {
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
    }

    public BotCommandConfig getConfig()
    {
        return cmdConfig;
    }
}
