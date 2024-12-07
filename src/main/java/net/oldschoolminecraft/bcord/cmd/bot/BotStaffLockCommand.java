package net.oldschoolminecraft.bcord.cmd.bot;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.oldschoolminecraft.bcord.Bridgecord;
import net.oldschoolminecraft.bcord.util.LinkData;
import net.oldschoolminecraft.bcord.util.StaffLockHandler;

import java.util.Objects;

public class BotStaffLockCommand extends BotCommand
{
    public BotStaffLockCommand()
    {
        super("slock");
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        if (!hasRoleByID(Objects.requireNonNull(event.getMember()), String.valueOf(config.getConfigOption("staffRoleID"))))
        {
            event.getMessage().reply("You do not have the required role to use this command!").queue();
            return;
        }

        LinkData linkData = Bridgecord.getInstance().getLinkHandler().loadLinkDataByID(event.getAuthor().getId());
        if (linkData == null)
        {
            event.getMessage().reply("Your account is not linked :(").queue();
            return;
        }

        if (!StaffLockHandler.getInstance().hasLock(linkData.username))
        {
            event.getMessage().reply("Your account is not locked :)").queue();
            return;
        }

        StaffLockHandler.getInstance().unlock(linkData.username);
        event.getMessage().reply("Your account has been temporarily unlocked!").queue();
    }
}
