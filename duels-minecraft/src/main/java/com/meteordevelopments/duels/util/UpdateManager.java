package com.meteordevelopments.duels.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.meteordevelopments.duels.DuelsPlugin;
import lombok.Getter;
import org.json.JSONObject;

@SuppressWarnings("all")
public class UpdateManager {
    private DuelsPlugin main;
    private URL spigotUrl;
    private boolean updateIsAvailable = false;
    private String currentVersion;
    @Getter
    private String latestVersion;
    
    // Regex pattern to parse semantic versions with optional pre-release suffixes
    // Supports X.Y, X.Y.Z, X.Y-suffix, X.Y.Z-suffix formats
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-(.+))?$");

    public UpdateManager(DuelsPlugin main) {
        this.main = main;
    }

    public void checkForUpdate() {
        // Check for the latest version from Spigot
        if (this.spigotUrl == null) {
            try {
                this.spigotUrl = new URL("https://version.itzadarsh-kushwaha.workers.dev/legacy/update.php");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        URLConnection spigotConnection = null;

        try {
            spigotConnection = this.spigotUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.latestVersion = (new BufferedReader(new InputStreamReader(spigotConnection.getInputStream()))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.latestVersion != null) {
            boolean isNewer = isNewVersionAvailable(this.getCurrentVersion(), this.latestVersion);
            if (isNewer) {
                main.getLogger().info("Update available: " + this.getCurrentVersion() + " -> " + this.latestVersion);
                this.setLatestVersion(this.latestVersion);
                this.setUpdateAvailability(true);
                if (main.getConfiguration().isStayUpToDate()) {
                    main.getLogger().info("Auto-downloading update...");
                    fetchAndDownloadFromModrinth();
                }
            } else {
                main.getLogger().fine("No update needed: " + this.getCurrentVersion() + " >= " + this.latestVersion);
            }
        }
    }

    /**
     * Compares two semantic versions to determine if an update is available.
     * Properly handles semantic versioning (X.Y.Z) and pre-release versions (X.Y.Z-suffix).
     * 
     * @param current The current version string
     * @param latest The latest version string
     * @return true if latest version is newer than current version, false otherwise
     */
    private boolean isNewVersionAvailable(String current, String latest) {
        try {
            int comparison = compareVersions(current, latest);
            return comparison < 0; // Return true if latest is greater than current
        } catch (Exception e) {
            // If parsing fails, don't update to avoid downgrading
            main.getLogger().warning("Failed to compare versions: " + current + " vs " + latest);
            return false;
        }
    }

    /**
     * Compares two semantic versions.
     * 
     * @param version1 First version to compare
     * @param version2 Second version to compare
     * @return Negative if version1 < version2, 0 if equal, Positive if version1 > version2
     * @throws IllegalArgumentException if versions are invalid
     */
    private int compareVersions(String version1, String version2) {
        VersionInfo v1 = parseVersion(version1);
        VersionInfo v2 = parseVersion(version2);
        
        // Compare major version
        if (v1.major != v2.major) {
            return Integer.compare(v1.major, v2.major);
        }
        
        // Compare minor version
        if (v1.minor != v2.minor) {
            return Integer.compare(v1.minor, v2.minor);
        }
        
        // Compare patch version
        if (v1.patch != v2.patch) {
            return Integer.compare(v1.patch, v2.patch);
        }
        
        // If one has pre-release and the other doesn't, release version is newer
        // (e.g., 1.0.0 > 1.0.0-dev)
        if (v1.isPrerelease && !v2.isPrerelease) {
            return -1; // v1 is pre-release, v2 is release, so v2 is newer
        }
        if (!v1.isPrerelease && v2.isPrerelease) {
            return 1; // v1 is release, v2 is pre-release, so v1 is newer
        }
        
        // Both are pre-releases or both are releases, compare pre-release tags
        if (v1.isPrerelease && v2.isPrerelease) {
            return v1.prerelease.compareTo(v2.prerelease);
        }
        
        // Versions are equal
        return 0;
    }

    /**
     * Parses a semantic version string into its components.
     * Supports formats: X, X.Y, X.Y.Z, and any with -suffix for pre-release.
     */
    private VersionInfo parseVersion(String versionString) {
        if (versionString == null || versionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }
        
        Matcher matcher = VERSION_PATTERN.matcher(versionString.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version format: " + versionString);
        }
        
        int major = Integer.parseInt(matcher.group(1));
        int minor = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        String prerelease = matcher.group(4);
        
        return new VersionInfo(major, minor, patch, prerelease);
    }

    /**
     * Internal class to store parsed version information.
     */
    private static class VersionInfo {
        final int major;
        final int minor;
        final int patch;
        final boolean isPrerelease;
        final String prerelease;
        
        VersionInfo(int major, int minor, int patch, String prerelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.prerelease = prerelease != null ? prerelease : "";
            this.isPrerelease = prerelease != null;
        }
    }

    private void fetchAndDownloadFromModrinth() {
        try {
            main.getLogger().info("Fetching download link for v" + this.latestVersion + " from Modrinth...");
            URL modrinthUrl = new URL("https://api.modrinth.com/v2/project/duels-optimised/version/" + this.latestVersion);
            URLConnection modrinthConnection = modrinthUrl.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(modrinthConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response
            String jsonResponse = response.toString();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String downloadUrl = jsonObject.getJSONArray("files").getJSONObject(0).getString("url");

            main.getLogger().info("Downloaded link obtained. Removing old plugin files...");
            // Delete old jar files
            deleteOldFiles("plugins", "Duels-Optimised");

            main.getLogger().info("Downloading Duels v" + this.latestVersion + "...");
            // Download the latest plugin
            downloadLatestPlugin(downloadUrl);
            main.getLogger().info("Update downloaded successfully! Please restart the server.");

        } catch (Exception e) {
            main.getLogger().severe("Failed to download update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteOldFiles(String directory, String prefix) {
        try (Stream<Path> paths = Files.list(Paths.get(directory))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadLatestPlugin(String downloadUrl) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream("plugins/Duels-Optimised-" + this.latestVersion + ".jar")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean updateIsAvailable() {
        return this.updateIsAvailable;
    }

    public void setUpdateAvailability(boolean availability) {
        this.updateIsAvailable = availability;
    }

    public String getCurrentVersion() {
        if (this.currentVersion == null) {
            this.currentVersion = this.main.getDescription().getVersion();
        }
        return this.currentVersion;
    }

    private void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
}