package me.realized.duels.api;

import me.realized.duels.Core;
import me.realized.duels.data.UserData;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 *
 * A static API for Duels.
 *
 * @author Realized
 *
 */

public class DuelsAPI {

    private static final Core instance = Core.getInstance();

    /**
     * @param uuid - UUID of the player to get userdata.
     *
     * @param force - Should we force the load from the files?
     *
     * @return UserData of the player if exists or null.
     */
    public static UserData getUser(UUID uuid, boolean force) {
        return instance.getDataManager().getUser(uuid, force);
    }

    /**
     * @param player - player to get userdata.
     *
     * @param force - Force the load from the files if not in cache?
     *
     * @return UserData of the player if exists or null.
     */
    public static UserData getUser(Player player, boolean force) {
        return instance.getDataManager().getUser(player.getUniqueId(), force);
    }

    /**
     *
     * @param player - player to check if in match.
     *
     * @return whether player is in match or not.
     */
    public static boolean isInMatch(Player player) {
        return instance.getArenaManager().isInMatch(player);
    }

    /**
     * @return version of the plugin.
     */
    public static String getVersion() {
        return instance.getDescription().getVersion();
    }
}
