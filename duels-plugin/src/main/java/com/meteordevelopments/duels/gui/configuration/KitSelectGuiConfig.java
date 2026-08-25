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
public class KitSelectGuiConfig {

    private String title = "&8Select Server Kit";
    private int rows = 3;
    private final List<Integer> itemSlots = new ArrayList<>();
    private final Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    private GuiItemConfig previousPageButton = new GuiItemConfig();
    private GuiItemConfig nextPageButton = new GuiItemConfig();
    private GuiItemConfig emptyButton = new GuiItemConfig();
    private GuiItemConfig backButton = new GuiItemConfig();
    private GuiItemConfig kitButton = new GuiItemConfig();
    private final Map<String, GuiItemConfig> extraButtons = new LinkedHashMap<>();

    public static KitSelectGuiConfig load(final FileConfiguration config) {
        final KitSelectGuiConfig kitConfig = new KitSelectGuiConfig();

        if (config == null) {
            return kitConfig;
        }

        kitConfig.setTitle(config.getString("title", "&8Select Server Kit"));
        kitConfig.setRows(Math.min(6, Math.max(1, config.getInt("rows", 3))));

        // Load dynamic item fill slots
        if (config.contains("item-slots")) {
            kitConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slots")));
        } else if (config.contains("item-slot")) {
            kitConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slot")));
        }

        // Load decorations
        final ConfigurationSection decSec = config.getConfigurationSection("decorations");
        if (decSec != null) {
            for (final String key : decSec.getKeys(false)) {
                final ConfigurationSection section = decSec.getConfigurationSection(key);
                if (section != null) {
                    kitConfig.getDecorations().put(key, GuiDecoration.parse(key, section));
                }
            }
        }

        // Load buttons
        final ConfigurationSection btnSec = config.getConfigurationSection("buttons");
        if (btnSec != null) {
            if (btnSec.isConfigurationSection("previous-page")) {
                kitConfig.setPreviousPageButton(GuiItemConfig.parse(btnSec.getConfigurationSection("previous-page")));
            }
            if (btnSec.isConfigurationSection("next-page")) {
                kitConfig.setNextPageButton(GuiItemConfig.parse(btnSec.getConfigurationSection("next-page")));
            }
            if (btnSec.isConfigurationSection("empty")) {
                kitConfig.setEmptyButton(GuiItemConfig.parse(btnSec.getConfigurationSection("empty")));
            }
            if (btnSec.isConfigurationSection("back")) {
                kitConfig.setBackButton(GuiItemConfig.parse(btnSec.getConfigurationSection("back")));
            }
            if (btnSec.isConfigurationSection("kit")) {
                kitConfig.setKitButton(GuiItemConfig.parse(btnSec.getConfigurationSection("kit")));
            }

            for (final String key : btnSec.getKeys(false)) {
                if (!key.equals("previous-page") && !key.equals("next-page")
                        && !key.equals("empty") && !key.equals("back") && !key.equals("kit")) {
                    final ConfigurationSection section = btnSec.getConfigurationSection(key);
                    if (section != null) {
                        kitConfig.getExtraButtons().put(key, GuiItemConfig.parse(section));
                    }
                }
            }
        }

        return kitConfig;
    }
}
