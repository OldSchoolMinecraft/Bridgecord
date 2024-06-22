package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BotMFACommand extends BotCommand
{
    public BotMFACommand()
    {
        super("mfa");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        String[] args = event.getMessage().getContentStripped().split(" ");

        if (args.length-1 < 1)
        {
            respond(event.getMessage(), "Usage: " + cmdPrefix + getConfig().getLabel() + " <enable/disable>", getConfig().shouldReply());
            return;
        }

        String flag = args[1];

        if (!stringMatches(flag, "enable", "disable"))
        {
            sendUsageMessage(event.getMessage(), getConfig().shouldReply());
            return;
        }

        boolean enable = flag.equalsIgnoreCase("enable");

        if (enable)
        {
            //TODO: enable MFA
            return;
        }

        //TODO: disable MFA
        String code = "1234"; //TODO: generate code
        queueDM(event.getAuthor(), "Please login to the server and run `/disable2fa " + code + "` to complete the process of turning off 2-factor authentication on your account.");
    }
}
