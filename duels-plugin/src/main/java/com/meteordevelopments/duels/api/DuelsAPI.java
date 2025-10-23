package com.meteordevelopments.duels.api;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.arena.ArenaManager;
import com.meteordevelopments.duels.api.user.UserManager;
import com.meteordevelopments.duels.data.UserData;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A static API for Duels.
 * (This is an old, deprecated API for Duels v2 and below.)
 *
 * @deprecated As of v3.0.0, use {@link Duels} instead.
 */
@Deprecated
public class DuelsAPI {


    /**
     * @deprecated As of v3.0.0, use {@link UserManager#get(UUID)} instead.
     */
    @Deprecated
    public static UserData getUser(final UUID uuid) {
        return DuelsPlugin.getInstance().getUserManager().get(uuid);
    }


    /**
     * @deprecated As of v3.0.0, use {@link UserManager#get(Player)} instead.
     */
    @Deprecated
    public static UserData getUser(final Player player) {
        return DuelsPlugin.getInstance().getUserManager().get(player);
    }


    /**
     * @deprecated As of v3.0.0, use {@link ArenaManager#isInMatch(Player)} instead.
     */
    @Deprecated
    public static boolean isInMatch(final Player player) {
        return DuelsPlugin.getInstance().getArenaManager().isInMatch(player);
    }


    /**
     * @deprecated As of v3.0.0, get the version from {@link Duels#getVersion()} instead.
     */
    @Deprecated
    public static String getVersion() {
        return DuelsPlugin.getInstance().getVersion();
    }
}