package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.util.DiscordLinkHandler;
import net.oldschoolminecraft.bcord.util.LinkData;

import java.util.Objects;

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

        DiscordLinkHandler linkHandler = Bridgecord.getInstance().getLinkHandler();

        String targetLinkName = args[1];

        if (args.length-1 == 2) // attempting staff override link
        {
            if (!hasRoleByID(Objects.requireNonNull(event.getMember()), String.valueOf(config.getConfigOption("staffRoleID"))))
            {
                respond(event.getMessage(), "You do not have permission to do this!", getConfig().shouldReply());
                return;
            }

            if (targetLinkName.startsWith("<@")) // they put the mention first instead of the username
            {
                respond(event.getMessage(), "Staff Override Usage: `!link <Minecraft Name> <Discord User Mention>`", getConfig().shouldReply());
                return;
            }

            if (args[1].equalsIgnoreCase("status")) // attempting to check link status
            {
                targetLinkName = args[2]; // bump username arg up a slot
                boolean isLinked = linkHandler.isAccountLinked(targetLinkName);
                String status = "**" + (isLinked ? "LINKED" : "NOT LINKED") + "**";
                respond(event.getMessage(), "The username you provided is: " + status, getConfig().shouldReply());
                return;
            }

            if (event.getMessage().getMentionedMembers().isEmpty()) // no mentions found in message
            {
                respond(event.getMessage(), "Staff Override Usage: `!link <Minecraft Name> <Discord User Mention>`", getConfig().shouldReply());
                return;
            }

            Member targetMember = event.getMessage().getMentionedMembers().get(0);
            linkHandler.getDataSource().linkDiscordAccount(targetLinkName, targetMember.getId());
            respond(event.getMessage(), String.format("**Link between `%s` and Discord ID `%s` has been forced!**", targetLinkName, targetMember.getId()), getConfig().shouldReply());
            return;
        }

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
