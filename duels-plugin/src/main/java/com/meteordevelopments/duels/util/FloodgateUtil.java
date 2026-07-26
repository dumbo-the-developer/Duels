package com.meteordevelopments.duels.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class for detecting Bedrock players via the Floodgate API.
 * All methods are safe to call even if Floodgate is not installed — they
 * will return false gracefully.
 */
public final class FloodgateUtil {

    private static Boolean floodgateAvailable;

    private FloodgateUtil() {
    }

    /**
     * @return true if the Floodgate plugin is present and enabled on this server
     */
    public static boolean isFloodgateAvailable() {
        if (floodgateAvailable == null) {
            floodgateAvailable = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        }
        return floodgateAvailable;
    }

    /**
     * Checks whether the given player is a Bedrock player connected via Geyser/Floodgate.
     *
     * @param player the player to check
     * @return true if the player is a Bedrock player, false if Java or if Floodgate is not installed
     */
    public static boolean isBedrockPlayer(final Player player) {
        if (!isFloodgateAvailable()) {
            return false;
        }

        try {
            return org.geysermc.floodgate.api.FloodgateApi.getInstance()
                    .isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable t) {
            // Floodgate classes not available at runtime — treat as Java player
            return false;
        }
    }

    /**
     * Resets the cached availability state. Called on plugin reload so the
     * check is re-evaluated.
     */
    public static void resetCache() {
        floodgateAvailable = null;
    }
}
