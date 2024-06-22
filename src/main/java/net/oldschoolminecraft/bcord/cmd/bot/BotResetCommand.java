package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.auth.AuthHandlerException;
import net.oldschoolminecraft.bcord.util.DiscordLinkHandler;
import net.oldschoolminecraft.bcord.util.LinkData;
import net.oldschoolminecraft.bcord.util.Util;

public class BotResetCommand extends BotCommand
{
    public BotResetCommand()
    {
        super("reset");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        DiscordLinkHandler linkHandler = Bridgecord.getInstance().getLinkHandler();
        LinkData data = linkHandler.loadLinkDataByID(event.getAuthor().getId());
        if (data == null)
        {
            respond(event.getMessage(), "Your account is not linked! Please use `" + cmdPrefix + config.getConfigOption("commands.link.label") + "` first!", getConfig().shouldReply());
            return;
        }

        String newPassword = Util.generateSecurePassword(); // 12 char alphanumeric, no special characters
        try
        {
            Util.selectAuthPlugin().updatePassword(data.username, newPassword);
        } catch (AuthHandlerException ex) {
            respond(event.getMessage(), "An unknown error has occurred while attempting to reset your password. Please try again later.", getConfig().shouldReply());
            return;
        }

        queueDM(event.getAuthor(), "Your in-game account has been updated with a new randomly generated password: `" + newPassword + "`. While we do not store these passwords, it is still recommended that you change it to something else. For added security, a password manager is encouraged (but not *required*).",
            (message) -> respond(event.getMessage(), "Success! We have dispatched a Direct Message.", getConfig().shouldReply()),
            (error) -> respond(event.getMessage(), "Error! We were not able to DM you :(", getConfig().shouldReply()));
    }
}
