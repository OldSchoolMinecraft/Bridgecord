package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.util.MessagePipe;
import net.oldschoolminecraft.bcord.util.ThrowablePipe;
import net.oldschoolminecraft.bcord.util.PluginConfig;

public abstract class BotCommand
{
    protected final PluginConfig config = Bridgecord.getInstance().getConfig();
    protected final String cmdPrefix = String.valueOf(config.getConfigOption("commands.prefix"));
    private final BotCommandConfig cmdConfig;
    private String usageMessage;

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
        queueDM(user, message, null, null);
    }

    public void queueDM(User user, String message, MessagePipe successPipe, ThrowablePipe errorPipe)
    {
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue((msg) -> {}, (exception) ->
        {
            if (errorPipe != null) errorPipe.flush(exception);
        });
    }

    public boolean stringMatches(String input, String... matches)
    {
        for (String match : matches) return input.equalsIgnoreCase(match);
        return false;
    }

    public void setUsageMessage(String message)
    {
        this.usageMessage = message;
    }

    public void sendUsageMessage(Message dcMsg, boolean reply)
    {
        if (reply) dcMsg.reply(usageMessage).queue();
        else dcMsg.getChannel().sendMessage(usageMessage).queue();
    }

    public boolean hasRoleByID(Member member, String roleId)
    {
        Role role = member.getGuild().getRoleById(roleId);
        return role != null && member.getRoles().contains(role);
    }

    public BotCommandConfig getConfig()
    {
        return cmdConfig;
    }
}
