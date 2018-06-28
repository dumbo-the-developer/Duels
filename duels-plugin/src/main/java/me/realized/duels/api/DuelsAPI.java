package me.realized.duels.api;

import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.user.UserManager;
import me.realized.duels.data.UserData;
import org.bukkit.entity.Player;

/**
 * A static API for Duels.
 *
 * @deprecated as of v3.0.0, use {@link Duels} instead.
 */

public class DuelsAPI {


    /**
     * @deprecated As of v3.0.0, use {@link UserManager#get(UUID)} instead.
     */
    @Deprecated
    public static UserData getUser(UUID uuid, boolean force) {
        return DuelsPlugin.getInstance().getUserManager().get(uuid);
    }


    /**
     * @deprecated As of v3.0.0, use {@link UserManager#get(Player)} instead.
     */
    @Deprecated
    public static UserData getUser(Player player, boolean force) {
        return DuelsPlugin.getInstance().getUserManager().get(player);
    }


    /**
     * @deprecated As of v3.0.0, use {@link ArenaManager#isInMatch(Player)} instead.
     */
    @Deprecated
    public static boolean isInMatch(Player player) {
        return DuelsPlugin.getInstance().getArenaManager().isInMatch(player);
    }


    /**
     * @deprecated As of v3.0.0, get the version from {@link Duels#getDescription()} instead.
     */
    @Deprecated
    public static String getVersion() {
        return DuelsPlugin.getInstance().getDescription().getVersion();
    }
}