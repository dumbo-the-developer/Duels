package com.meteordevelopments.duels.gui.configuration;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Reloadable;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class GuiConfigManager implements Loadable, Reloadable {

    private final DuelsPlugin plugin;
    private final File guiFolder;

    @Getter
    private SettingsGuiConfig settingsGuiConfig;
    @Getter
    private KitTypeSelectGuiConfig kitTypeSelectGuiConfig;
    @Getter
    private ArenaSelectGuiConfig arenaSelectGuiConfig;
    @Getter
    private KitSelectGuiConfig kitSelectGuiConfig;
    @Getter
    private CustomKitSelectGuiConfig customKitSelectGuiConfig;
    @Getter
    private QueueSelectGuiConfig queueSelectGuiConfig;

    public GuiConfigManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.guiFolder = new File(plugin.getDataFolder(), "GUI");
    }

    @Override
    public void handleLoad() throws Exception {
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }

        loadSettingsGui();
        loadKitTypeSelectGui();
        loadArenaSelectGui();
        loadKitSelectGui();
        loadCustomKitSelectGui();
        loadQueueSelectGui();

        if (plugin.getSettingManager() != null) {
            plugin.getSettingManager().handleUnload();
        }
    }

    @Override
    public void handleUnload() {
        settingsGuiConfig = null;
        kitTypeSelectGuiConfig = null;
        arenaSelectGuiConfig = null;
        kitSelectGuiConfig = null;
        customKitSelectGuiConfig = null;
        queueSelectGuiConfig = null;
    }

    private void loadSettingsGui() {
        final File file = new File(guiFolder, "settings.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("GUI/settings.yml", false);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                plugin.getLogger().warning("Could not extract GUI/settings.yml from jar: " + ex.getMessage());
            }
        }

        final FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from internal resource if any keys are missing
        final InputStream defaultStream = plugin.getResource("GUI/settings.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        this.settingsGuiConfig = SettingsGuiConfig.load(yaml);
    }

    private void loadKitTypeSelectGui() {
        final File file = new File(guiFolder, "kit_type_selector.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("GUI/kit_type_selector.yml", false);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                plugin.getLogger().warning("Could not extract GUI/kit_type_selector.yml from jar: " + ex.getMessage());
            }
        }

        final FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from internal resource if any keys are missing
        final InputStream defaultStream = plugin.getResource("GUI/kit_type_selector.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        this.kitTypeSelectGuiConfig = KitTypeSelectGuiConfig.load(yaml);
    }

    private void loadArenaSelectGui() {
        final File file = new File(guiFolder, "arena_selector.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("GUI/arena_selector.yml", false);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                plugin.getLogger().warning("Could not extract GUI/arena_selector.yml from jar: " + ex.getMessage());
            }
        }

        final FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from internal resource if any keys are missing
        final InputStream defaultStream = plugin.getResource("GUI/arena_selector.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        this.arenaSelectGuiConfig = ArenaSelectGuiConfig.load(yaml);
    }

    private void loadKitSelectGui() {
        final File file = new File(guiFolder, "kit_selector.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("GUI/kit_selector.yml", false);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                plugin.getLogger().warning("Could not extract GUI/kit_selector.yml from jar: " + ex.getMessage());
            }
        }

        final FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from internal resource if any keys are missing
        final InputStream defaultStream = plugin.getResource("GUI/kit_selector.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        this.kitSelectGuiConfig = KitSelectGuiConfig.load(yaml);
    }

    private void loadCustomKitSelectGui() {
        final File file = new File(guiFolder, "custom_kit_selector.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("GUI/custom_kit_selector.yml", false);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                plugin.getLogger().warning("Could not extract GUI/custom_kit_selector.yml from jar: " + ex.getMessage());
            }
        }

        final FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from internal resource if any keys are missing
        final InputStream defaultStream = plugin.getResource("GUI/custom_kit_selector.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        this.customKitSelectGuiConfig = CustomKitSelectGuiConfig.load(yaml);
    }

    private void loadQueueSelectGui() {
        final File file = new File(guiFolder, "queue_selector.yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("GUI/queue_selector.yml", false);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                plugin.getLogger().warning("Could not extract GUI/queue_selector.yml from jar: " + ex.getMessage());
            }
        }

        final FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from internal resource if any keys are missing
        final InputStream defaultStream = plugin.getResource("GUI/queue_selector.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        this.queueSelectGuiConfig = QueueSelectGuiConfig.load(yaml);
    }
}
