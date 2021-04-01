package me.realized.duels.util.compat;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public final class Skulls extends CompatBase {

    private static final Map<UUID, GameProfile> cache = new HashMap<>();

    public static void setProfile(final SkullMeta meta, final Player player) {
        GameProfile cached = cache.get(player.getUniqueId());
        try {
            if (cached == null) {
                final Object nmsPlayer = GET_HANDLE.invoke(player);
                cached = (GameProfile) GET_PROFILE.invoke(nmsPlayer);
                cache.put(player.getUniqueId(), cached);
            }

            PROFILE.set(meta, cached);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private Skulls() {}
}
