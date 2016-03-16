package me.realized.duels.utilities;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileUtil {

    private static final Map<String, Profile> uuidMap = new HashMap<>();

    @SuppressWarnings("deprecation")
    public static UUID getUUID(String username) {
        Profile profile = get(username);

        if (profile != null) {
            return profile.getUUID();
        }

        if (Bukkit.getOnlineMode() || Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            if (Bukkit.getPlayerExact(username) != null) {
                return Bukkit.getPlayerExact(username).getUniqueId();
            }

            if (Bukkit.getOfflinePlayer(username).hasPlayedBefore()) {
                UUID uuid = Bukkit.getOfflinePlayer(username).getUniqueId();
                place(username, uuid);
                return uuid;
            }
        }

        return null;
    }

    private static void place(String name, UUID uuid) {
        uuidMap.put(name.toLowerCase(), new Profile(uuid));
    }

    private static Profile get(String name) {
        Profile profile = uuidMap.get(name.toLowerCase());

        if (profile == null) {
            return null;
        }

        if (profile.getTime() + 1000 * 300 - System.currentTimeMillis() <= 0) {
            return null;
        }

        return profile;
    }

    private static class Profile {

        private final long time;
        private final UUID uuid;

        public Profile(UUID uuid) {
            this.time = System.currentTimeMillis();
            this.uuid = uuid;
        }

        public long getTime() {
            return time;
        }

        public UUID getUUID() {
            return uuid;
        }
    }
}
