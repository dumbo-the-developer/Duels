package me.realized.duels.api;

import javax.annotation.Nonnull;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import org.bukkit.plugin.Plugin;

public interface Duels extends Plugin {

    // TODO: 29/06/2018 #getCommand and command package for API? DuelsExtension-Leaderboards will need to reg a sub command.

    @Nonnull
    UserManager getUserManager();

    @Nonnull
    ArenaManager getArenaManager();

    @Nonnull
    KitManager getKitManager();

    /**
     * Reloads the plugin.
     *
     * @return true if reload was successful, otherwise false
     */
    boolean reload();

}
