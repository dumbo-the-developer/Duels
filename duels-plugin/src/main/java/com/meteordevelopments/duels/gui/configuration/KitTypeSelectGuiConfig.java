package com.meteordevelopments.duels.gui.configuration;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class KitTypeSelectGuiConfig {

    private String title = "&8Select Kit Type";
    private int rows = 4;
    private final Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    private GuiItemConfig serverKitsButton = new GuiItemConfig();
    private GuiItemConfig customKitsButton = new GuiItemConfig();
    private GuiItemConfig ownInventoryButton = new GuiItemConfig();
    private GuiItemConfig backButton = new GuiItemConfig();
    private final Map<String, GuiItemConfig> extraButtons = new LinkedHashMap<>();

    public static KitTypeSelectGuiConfig load(final FileConfiguration config) {
        final KitTypeSelectGuiConfig typeConfig = new KitTypeSelectGuiConfig();

        if (config == null) {
            return typeConfig;
        }

        typeConfig.setTitle(config.getString("title", "&8Select Kit Type"));
        typeConfig.setRows(Math.min(6, Math.max(1, config.getInt("rows", 4))));

        // Load decorations
        final ConfigurationSection decSec = config.getConfigurationSection("decorations");
        if (decSec != null) {
            for (final String key : decSec.getKeys(false)) {
                final ConfigurationSection section = decSec.getConfigurationSection(key);
                if (section != null) {
                    typeConfig.getDecorations().put(key, GuiDecoration.parse(key, section));
                }
            }
        }

        // Load buttons
        final ConfigurationSection btnSec = config.getConfigurationSection("buttons");
        if (btnSec != null) {
            if (btnSec.isConfigurationSection("server-kits")) {
                typeConfig.setServerKitsButton(GuiItemConfig.parse(btnSec.getConfigurationSection("server-kits")));
            }
            if (btnSec.isConfigurationSection("custom-kits")) {
                typeConfig.setCustomKitsButton(GuiItemConfig.parse(btnSec.getConfigurationSection("custom-kits")));
            }
            if (btnSec.isConfigurationSection("own-inventory")) {
                typeConfig.setOwnInventoryButton(GuiItemConfig.parse(btnSec.getConfigurationSection("own-inventory")));
            }
            if (btnSec.isConfigurationSection("back")) {
                typeConfig.setBackButton(GuiItemConfig.parse(btnSec.getConfigurationSection("back")));
            }

            for (final String key : btnSec.getKeys(false)) {
                if (!key.equals("server-kits") && !key.equals("custom-kits")
                        && !key.equals("own-inventory") && !key.equals("back")) {
                    final ConfigurationSection section = btnSec.getConfigurationSection(key);
                    if (section != null) {
                        typeConfig.getExtraButtons().put(key, GuiItemConfig.parse(section));
                    }
                }
            }
        }

        return typeConfig;
    }
}
