package me.realized.duels.util.compat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Players extends CompatBase {

    private Players() {}

    /**
     * Handles {@link Bukkit#getOnlinePlayers()} returning Player[] in old versions of spigot
     */
    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            return Bukkit.getOnlinePlayers();
        } catch (NoSuchMethodError ignored) {}

        if (GET_ONLINE_PLAYERS != null) {
            try {
                final Object result = GET_ONLINE_PLAYERS.invoke(null);

                if (result != null && result instanceof Player[]) {
                    return Arrays.asList((Player[]) result);
                }
            } catch (Exception ignored) {}
        }

        return Collections.emptyList();
    }
}
