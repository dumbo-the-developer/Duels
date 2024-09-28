package com.meteordevelopments.duels.util.compat;

import lombok.Getter;
import com.meteordevelopments.duels.util.compat.interfaces.ItemsCompat;
import org.bukkit.Bukkit;

public class ItemsFactory {

    @Getter
    private static ItemsCompat itemsCompat;

    static {
        String version = getMinecraftVersion();
        if (isVersionEqualOrAbove(version, "1.13")) {
            itemsCompat = new ItemsAboveV_1_13();  // For versions 1.13 and above
        } else if (isVersionEqualOrAbove(version, "1.9")) {
            itemsCompat = new ItemsBelowV_1_13();  // For versions between 1.9 and 1.12
        } else {
            itemsCompat = new ItemsLegacy(); // For versions below 1.9
        }
    }

    private static String getMinecraftVersion() {
        // Example to get the Minecraft server version
        return Bukkit.getServer().getBukkitVersion(); // Returns something like "1.20.6"
    }

    public static boolean isVersionEqualOrAbove(String version, String targetVersion) {
        String[] versionParts = version.split("\\.");
        String[] targetParts = targetVersion.split("\\.");

        for (int i = 0; i < Math.max(versionParts.length, targetParts.length); i++) {
            int vPart = i < versionParts.length ? Integer.parseInt(versionParts[i]) : 0;
            int tPart = i < targetParts.length ? Integer.parseInt(targetParts[i]) : 0;

            if (vPart > tPart) {
                return true;
            } else if (vPart < tPart) {
                return false;
            }
        }
        return true; // versions are equal
    }

    // Overloaded method to check against the server's current version
    public static boolean isCurrentVersionEqualOrAbove(String targetVersion) {
        return isVersionEqualOrAbove(getMinecraftVersion(), targetVersion);
    }
}
