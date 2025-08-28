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

    public UpdateManager(DuelsPlugin main) {
        this.main = main;
    }

    public void checkForUpdate() {
        // Check for the latest version from Spigot
        if (this.spigotUrl == null) {
            try {
                this.spigotUrl = new URL("https://meteordevelopments.com/legacy/update.php");
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

        if (this.latestVersion != null && !this.getCurrentVersion().equals(this.latestVersion)) {
            this.setLatestVersion(this.latestVersion);
            this.setUpdateAvailability(true);
            if (main.getConfiguration().isStayUpToDate()) {
                fetchAndDownloadFromModrinth();
            }
        }
    }

    private void fetchAndDownloadFromModrinth() {
        try {
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

            // Delete old jar files
            deleteOldFiles("plugins", "Duels-Optimised");

            // Download the latest plugin
            downloadLatestPlugin(downloadUrl);

        } catch (Exception e) {
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