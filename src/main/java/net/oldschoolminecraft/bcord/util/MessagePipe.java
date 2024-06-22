package net.oldschoolminecraft.bcord.util;

import net.dv8tion.jda.api.entities.Message;

public interface MessagePipe
{
    void flush(Message message);
}
