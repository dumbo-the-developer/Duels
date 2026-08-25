package com.meteordevelopments.duels.gui.configuration;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class SettingsGuiConfig {

    private String title = "&8Request Settings";
    private int rows = 6;
    private final Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    private GuiItemConfig detailsButton = new GuiItemConfig();
    private GuiItemConfig kitSelectButton = new GuiItemConfig();
    private GuiItemConfig ownInventoryButton = new GuiItemConfig();
    private GuiItemConfig arenaSelectButton = new GuiItemConfig();
    private GuiItemConfig itemBettingButton = new GuiItemConfig();
    private GuiItemConfig sendButton = new GuiItemConfig();
    private GuiItemConfig cancelButton = new GuiItemConfig();
    private final Map<String, GuiItemConfig> extraButtons = new LinkedHashMap<>();

    public static SettingsGuiConfig load(final FileConfiguration config) {
        final SettingsGuiConfig settingsConfig = new SettingsGuiConfig();

        if (config == null) {
            return settingsConfig;
        }

        settingsConfig.setTitle(config.getString("title", "&8Request Settings"));
        settingsConfig.setRows(Math.min(6, Math.max(1, config.getInt("rows", 6))));

        // Load decorations
        final ConfigurationSection decSec = config.getConfigurationSection("decorations");
        if (decSec != null) {
            for (final String key : decSec.getKeys(false)) {
                final ConfigurationSection section = decSec.getConfigurationSection(key);
                if (section != null) {
                    settingsConfig.getDecorations().put(key, GuiDecoration.parse(key, section));
                }
            }
        }

        // Load buttons
        final ConfigurationSection btnSec = config.getConfigurationSection("buttons");
        if (btnSec != null) {
            if (btnSec.isConfigurationSection("details")) {
                settingsConfig.setDetailsButton(GuiItemConfig.parse(btnSec.getConfigurationSection("details")));
            }
            if (btnSec.isConfigurationSection("kit-selector")) {
                settingsConfig.setKitSelectButton(GuiItemConfig.parse(btnSec.getConfigurationSection("kit-selector")));
            }
            if (btnSec.isConfigurationSection("own-inventory")) {
                settingsConfig.setOwnInventoryButton(GuiItemConfig.parse(btnSec.getConfigurationSection("own-inventory")));
            }
            if (btnSec.isConfigurationSection("arena-selector")) {
                settingsConfig.setArenaSelectButton(GuiItemConfig.parse(btnSec.getConfigurationSection("arena-selector")));
            }
            if (btnSec.isConfigurationSection("item-betting")) {
                settingsConfig.setItemBettingButton(GuiItemConfig.parse(btnSec.getConfigurationSection("item-betting")));
            }
            if (btnSec.isConfigurationSection("send")) {
                settingsConfig.setSendButton(GuiItemConfig.parse(btnSec.getConfigurationSection("send")));
            }
            if (btnSec.isConfigurationSection("cancel")) {
                settingsConfig.setCancelButton(GuiItemConfig.parse(btnSec.getConfigurationSection("cancel")));
            }

            for (final String key : btnSec.getKeys(false)) {
                if (!key.equals("details") && !key.equals("kit-selector") && !key.equals("own-inventory")
                        && !key.equals("arena-selector") && !key.equals("item-betting")
                        && !key.equals("send") && !key.equals("cancel")) {
                    final ConfigurationSection section = btnSec.getConfigurationSection(key);
                    if (section != null) {
                        settingsConfig.getExtraButtons().put(key, GuiItemConfig.parse(section));
                    }
                }
            }
        }

        return settingsConfig;
    }
}
