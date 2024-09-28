package com.meteordevelopments.duels.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.meteordevelopments.duels.DuelsPlugin;
import lombok.Getter;

public class UpdateManager {
    private DuelsPlugin main;
    private URL url;
    private int pluginId = 118881;
    private boolean updateIsAvailable = false;
    private String currentVersion;
    @Getter
    private String latestVersion;

    public UpdateManager(DuelsPlugin main) {
        this.main = main;
    }

    public void checkForUpdate() {
        if (this.url == null) {
            try {
                this.url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.pluginId);
            } catch (Exception var5) {
            }
        }

        URLConnection connection = null;

        try {
            connection = this.url.openConnection();
        } catch (IOException ignored) {
        }

        try {
            this.latestVersion = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
        } catch (IOException ignored) {
        }

        if (this.latestVersion != null && !this.getCurrentVersion().equals(this.latestVersion)) {
            this.setLatestVersion(this.latestVersion);
            this.setUpdateAvailability(true);
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
            this.currentVersion = "v" + this.main.getDescription().getVersion();
        }

        return this.currentVersion;
    }

    private void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

}