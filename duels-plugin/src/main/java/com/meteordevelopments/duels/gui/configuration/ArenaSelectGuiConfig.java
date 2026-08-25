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
public class ArenaSelectGuiConfig {

    private String title = "&8Arena Selection";
    private int rows = 4;
    private final List<Integer> itemSlots = new ArrayList<>();
    private final Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    private GuiItemConfig previousPageButton = new GuiItemConfig();
    private GuiItemConfig nextPageButton = new GuiItemConfig();
    private GuiItemConfig emptyButton = new GuiItemConfig();
    private GuiItemConfig backButton = new GuiItemConfig();
    private GuiItemConfig arenaButton = new GuiItemConfig();
    private String arenaAvailableText = "&aAvailable";
    private String arenaUnavailableText = "&cUnavailable";
    private final Map<String, GuiItemConfig> extraButtons = new LinkedHashMap<>();

    public static ArenaSelectGuiConfig load(final FileConfiguration config) {
        final ArenaSelectGuiConfig arenaConfig = new ArenaSelectGuiConfig();

        if (config == null) {
            return arenaConfig;
        }

        arenaConfig.setTitle(config.getString("title", "&8Arena Selection"));
        arenaConfig.setRows(Math.min(6, Math.max(1, config.getInt("rows", 4))));

        // Load item fill slots
        if (config.contains("item-slots")) {
            arenaConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slots")));
        } else if (config.contains("item-slot")) {
            arenaConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slot")));
        }

        // Load decorations
        final ConfigurationSection decSec = config.getConfigurationSection("decorations");
        if (decSec != null) {
            for (final String key : decSec.getKeys(false)) {
                final ConfigurationSection section = decSec.getConfigurationSection(key);
                if (section != null) {
                    arenaConfig.getDecorations().put(key, GuiDecoration.parse(key, section));
                }
            }
        }

        // Load buttons
        final ConfigurationSection btnSec = config.getConfigurationSection("buttons");
        if (btnSec != null) {
            if (btnSec.isConfigurationSection("previous-page")) {
                arenaConfig.setPreviousPageButton(GuiItemConfig.parse(btnSec.getConfigurationSection("previous-page")));
            }
            if (btnSec.isConfigurationSection("next-page")) {
                arenaConfig.setNextPageButton(GuiItemConfig.parse(btnSec.getConfigurationSection("next-page")));
            }
            if (btnSec.isConfigurationSection("empty")) {
                arenaConfig.setEmptyButton(GuiItemConfig.parse(btnSec.getConfigurationSection("empty")));
            }
            if (btnSec.isConfigurationSection("back")) {
                arenaConfig.setBackButton(GuiItemConfig.parse(btnSec.getConfigurationSection("back")));
            }
            if (btnSec.isConfigurationSection("arena")) {
                final ConfigurationSection arenaSec = btnSec.getConfigurationSection("arena");
                arenaConfig.setArenaButton(GuiItemConfig.parse(arenaSec));
                arenaConfig.setArenaAvailableText(arenaSec.getString("lore-available", "&aAvailable"));
                arenaConfig.setArenaUnavailableText(arenaSec.getString("lore-unavailable", "&cUnavailable"));
            }

            for (final String key : btnSec.getKeys(false)) {
                if (!key.equals("previous-page") && !key.equals("next-page")
                        && !key.equals("empty") && !key.equals("back") && !key.equals("arena")) {
                    final ConfigurationSection section = btnSec.getConfigurationSection(key);
                    if (section != null) {
                        arenaConfig.getExtraButtons().put(key, GuiItemConfig.parse(section));
                    }
                }
            }
        }

        return arenaConfig;
    }
}
