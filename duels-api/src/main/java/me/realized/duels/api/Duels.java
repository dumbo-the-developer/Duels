package me.realized.duels.api;

import javax.annotation.Nonnull;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import org.bukkit.plugin.Plugin;

public interface Duels extends Plugin {

    @Nonnull
    UserManager getUserManager();

    @Nonnull
    ArenaManager getArenaManager();

    @Nonnull
    KitManager getKitManager();


    /**
     * @param command Name of the parent command to register the sub command
     * @param subCommand SubCommand to register
     * @return true if sub command was successfully registered, otherwise false
     */
    boolean registerSubCommand(@Nonnull final String command, @Nonnull final SubCommand subCommand);


    /**
     * Reloads the plugin.
     *
     * @return true if reload was successful, otherwise false
     */
    boolean reload();
}
