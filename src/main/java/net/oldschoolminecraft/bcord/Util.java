package net.oldschoolminecraft.bcord;

import net.oldschoolminecraft.bcord.auth.AuthMeHandler;
import net.oldschoolminecraft.bcord.auth.AuthPluginHandler;
import net.oldschoolminecraft.bcord.auth.OSASHandler;
import net.oldschoolminecraft.bcord.auth.xAuthHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util
{
    private static final List<AuthPluginHandler> SUPPORTED_AUTH_HANDLERS = new ArrayList<>();

    static
    {
        try
        {
            SUPPORTED_AUTH_HANDLERS.add(new OSASHandler());
        } catch (NoClassDefFoundError ignored) {} // not installed

        try
        {
            SUPPORTED_AUTH_HANDLERS.add(new AuthMeHandler());
        } catch (NoClassDefFoundError ignored) {} // not installed

        try
        {
            SUPPORTED_AUTH_HANDLERS.add(new xAuthHandler());
        } catch (NoClassDefFoundError ignored) {} // not installed
    }

    public static String stripUnprocessedColor(String input)
    {
        return input == null ? null : input.replaceAll("(?i)&[0-F]", "");
    }

    public static AuthPluginHandler selectAuthPlugin()
    {
        return SUPPORTED_AUTH_HANDLERS.stream()
                .filter(AuthPluginHandler::isInstalled)
                .reduce((first, second) -> {
                    throw new RuntimeException("Multiple auth plugins are installed. Please remove one of them.");
                })
                .orElseThrow(() -> new RuntimeException("No auth plugin is installed."));
    }
}
