package com.meteordevelopments.duels.gui.configuration;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CustomKitSelectGuiConfig {

    private String title = "&8Select Custom Kit";
    private int rows = 4;
    private final List<Integer> itemSlots = new ArrayList<>();
    private final Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    private GuiItemConfig emptyButton = new GuiItemConfig();
    private GuiItemConfig backButton = new GuiItemConfig();
    private GuiItemConfig customKitButton = new GuiItemConfig();
    private final Map<String, GuiItemConfig> extraButtons = new LinkedHashMap<>();

    public static CustomKitSelectGuiConfig load(final FileConfiguration config) {
        final CustomKitSelectGuiConfig customConfig = new CustomKitSelectGuiConfig();

        if (config == null) {
            return customConfig;
        }

        customConfig.setTitle(config.getString("title", "&8Select Custom Kit"));
        customConfig.setRows(Math.min(6, Math.max(1, config.getInt("rows", 4))));

        // Load item fill slots
        if (config.contains("item-slots")) {
            customConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slots")));
        } else if (config.contains("item-slot")) {
            customConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slot")));
        }

        // Load decorations
        final ConfigurationSection decSec = config.getConfigurationSection("decorations");
        if (decSec != null) {
            for (final String key : decSec.getKeys(false)) {
                final ConfigurationSection section = decSec.getConfigurationSection(key);
                if (section != null) {
                    customConfig.getDecorations().put(key, GuiDecoration.parse(key, section));
                }
            }
        }

        // Load buttons
        final ConfigurationSection btnSec = config.getConfigurationSection("buttons");
        if (btnSec != null) {
            if (btnSec.isConfigurationSection("empty")) {
                customConfig.setEmptyButton(GuiItemConfig.parse(btnSec.getConfigurationSection("empty")));
            }
            if (btnSec.isConfigurationSection("back")) {
                customConfig.setBackButton(GuiItemConfig.parse(btnSec.getConfigurationSection("back")));
            }
            if (btnSec.isConfigurationSection("custom-kit")) {
                customConfig.setCustomKitButton(GuiItemConfig.parse(btnSec.getConfigurationSection("custom-kit")));
            }

            for (final String key : btnSec.getKeys(false)) {
                if (!key.equals("empty") && !key.equals("back") && !key.equals("custom-kit")) {
                    final ConfigurationSection section = btnSec.getConfigurationSection(key);
                    if (section != null) {
                        customConfig.getExtraButtons().put(key, GuiItemConfig.parse(section));
                    }
                }
            }
        }

        return customConfig;
    }
}
