package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.util.DiscordLinkHandler;
import net.oldschoolminecraft.bcord.util.LinkData;

public class BotLinkCommand extends BotCommand
{
    public BotLinkCommand()
    {
        super("link");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        String[] args = event.getMessage().getContentStripped().split(" ");

        if (args.length-1 < 1)
        {
            respond(event.getMessage(), "You must specify a username! Example: `" + cmdPrefix + getConfig().getLabel() + " ExamplePlayer123`", getConfig().shouldReply());
            return;
        }

        String targetLinkName = args[1];

        DiscordLinkHandler linkHandler = Bridgecord.getInstance().getLinkHandler();
        LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
        if (data != null)
        {
            respond(event.getMessage(), "You are already linked to `" + data.username + "` on this server :)", getConfig().shouldReply());
            return;
        }

        try
        {
            String code = linkHandler.startLinkProcess(targetLinkName, event.getAuthor().getId());

            queueDM(event.getAuthor(), "You have successfully initiated the linking process! Please connect to the server with the username you provided (`" + targetLinkName + "`), and enter the following into the chat: `/dlink " + code + "`.\r\rIf the username shown in this message is not correct (and exact, it's case-sensitive!), please try linking again with the correct username.",
                (message) -> respond(event.getMessage(), "Success! We have dispatched a Direct Message.", getConfig().shouldReply()),
                (error) -> respond(event.getMessage(), "Error! We were not able to DM you :(", getConfig().shouldReply()));
        } catch (Exception ex) {
            respond(event.getMessage(), "Error: " + ex.getMessage(), getConfig().shouldReply());
            ex.printStackTrace(System.err);
        }
    }
}
