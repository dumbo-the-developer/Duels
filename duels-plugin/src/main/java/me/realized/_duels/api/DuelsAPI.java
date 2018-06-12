package me.realized._duels.api;

import java.util.UUID;
import me.realized._duels.Core;
import me.realized._duels.data.UserData;
import org.bukkit.entity.Player;

/**
 *
 * @author Realized
 * @deprecated As of v3.0.0. Use {@link Core} instead.
 *
 * A static API for Duels.
 */

@Deprecated
public class DuelsAPI {

    private static final Core instance = Core.getInstance();

    /**
     * @param uuid - UUID of the player to get userdata.
     * @param force - Should we force the load from the files?
     * @return UserData of the player if exists or null.
     */
    @Deprecated
    public static UserData getUser(UUID uuid, boolean force) {
        return instance.getDataManager().getUser(uuid, force);
    }

    /**
     * @param player - player to get userdata.
     * @param force - Force the load from the files if not in cache?
     * @return UserData of the player if exists or null.
     */
    @Deprecated
    public static UserData getUser(Player player, boolean force) {
        return instance.getDataManager().getUser(player.getUniqueId(), force);
    }

    /**
     * @param player - player to check if in match.
     * @return true if player is in match, false otherwise.
     */
    @Deprecated
    public static boolean isInMatch(Player player) {
        return instance.getArenaManager().isInMatch(player);
    }

    /**
     * @return version string of the plugin.
     */
    @Deprecated
    public static String getVersion() {
        return instance.getDescription().getVersion();
    }
}
