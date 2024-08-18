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
        for (Role role : Objects.requireNonNull(event.getMember()).getRoles())
        {
            if (role.getId().equals(String.valueOf(Bridgecord.getInstance().getConfig().getConfigOption("staffRoleID"))))
            {
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
                return;
            }
        }

        event.getMessage().reply("You do not have the required role to run this command.").queue();
    }
}
