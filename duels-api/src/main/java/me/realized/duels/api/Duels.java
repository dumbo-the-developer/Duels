package me.realized.duels.api;

import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;

public interface Duels {

    UserManager getUserManager();

    ArenaManager getArenaManager();

    KitManager getKitManager();

    /**
     * Reloads the plugin.
     *
     * @return true if reload was successful, otherwise false
     */
    boolean reload();

}
