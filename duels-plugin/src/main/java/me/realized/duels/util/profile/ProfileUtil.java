package me.realized.duels.util.profile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ProfileUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    private static boolean USING_SPIGOT;

    static {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            USING_SPIGOT = true;
        } catch (ClassNotFoundException ignored) {}
    }

    private ProfileUtil() {}

    public static boolean isOnlineMode() {
        final boolean online;
        return !(online = Bukkit.getOnlineMode()) && USING_SPIGOT && Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")
            || online;
    }

    public static boolean isUUID(final String s) {
        return UUID_PATTERN.matcher(s).matches();
    }

    public static void getNames(final List<UUID> uuids, final Consumer<Map<UUID, String>> consumer) {
        NameFetcher.getNames(uuids, consumer);
    }

    public static void getUUID(final String name, final Consumer<String> consumer, final Consumer<String> errorHandler) {
        final Player player;

        if ((player = Bukkit.getPlayerExact(name)) != null) {
            consumer.accept(player.getUniqueId().toString());
            return;
        }

        try {
            consumer.accept(UUIDFetcher.getUUID(name));
        } catch (Exception ex) {
            errorHandler.accept(ex.getMessage());
        }
    }
}
