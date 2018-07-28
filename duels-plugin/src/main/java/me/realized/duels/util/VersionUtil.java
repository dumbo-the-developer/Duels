package me.realized.duels.util;

public final class VersionUtil {

    public static boolean isLower(String version, String otherVersion) {
        version = version.replace("-SNAPSHOT", "").replace(".", "");
        otherVersion = otherVersion.replace("-SNAPSHOT", "").replace(".", "");
        return NumberUtil.parseInt(version).orElse(0) < NumberUtil.parseInt(otherVersion).orElse(0);
    }

    private VersionUtil() {}
}
