package net.oldschoolminecraft.bcord.hooks;

import io.github.aleksandarharalanov.chatguard.ChatGuard;
import io.github.aleksandarharalanov.chatguard.listener.PlayerChatListener;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerChatEvent;

public class CGUtils
{
    private ChatGuard cg;
    private PlayerChatListener chatListener;

    public CGUtils()
    {
        cg = (ChatGuard) Bukkit.getServer().getPluginManager().getPlugin("ChatGuard");
        chatListener = new PlayerChatListener();
    }

    public boolean isEventCancelled(PlayerChatEvent event)
    {
        chatListener.onPlayerChat(event);
        return event.isCancelled();
    }
}
