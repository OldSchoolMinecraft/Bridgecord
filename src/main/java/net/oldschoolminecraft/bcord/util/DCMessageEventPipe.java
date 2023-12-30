package net.oldschoolminecraft.bcord.util;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface DCMessageEventPipe
{
    void flush(MessageReceivedEvent event);
}
