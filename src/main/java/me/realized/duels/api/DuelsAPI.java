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
     * @return UserData
     */
    public static UserData getUser(UUID uuid, boolean force) {
        return instance.getDataManager().getUser(uuid, force);
    }

    /**
     * @param player - player to get userdata.
     *
     * @param force - Should we force the load from the files?
     *
     * @return UserData
     */
    public static UserData getUser(Player player, boolean force) {
        return instance.getDataManager().getUser(player.getUniqueId(), force);
    }

    /**
     *
     * @param player - player to check if in match.
     *
     * @return boolean
     */
    public static boolean isInMatch(Player player) {
        return instance.getArenaManager().isInMatch(player);
    }
}
