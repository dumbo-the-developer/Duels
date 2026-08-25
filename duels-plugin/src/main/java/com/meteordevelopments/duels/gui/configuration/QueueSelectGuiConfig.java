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
public class QueueSelectGuiConfig {

    private String title = "&8Duel Queues";
    private int rows = 4;
    private final List<Integer> itemSlots = new ArrayList<>();
    private final Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    private GuiItemConfig previousPageButton = new GuiItemConfig();
    private GuiItemConfig nextPageButton = new GuiItemConfig();
    private GuiItemConfig emptyButton = new GuiItemConfig();
    private GuiItemConfig queueButton = new GuiItemConfig();
    private final Map<String, GuiItemConfig> extraButtons = new LinkedHashMap<>();

    public static QueueSelectGuiConfig load(final FileConfiguration config) {
        final QueueSelectGuiConfig queueConfig = new QueueSelectGuiConfig();

        if (config == null) {
            return queueConfig;
        }

        queueConfig.setTitle(config.getString("title", "&8Duel Queues"));
        queueConfig.setRows(Math.min(6, Math.max(1, config.getInt("rows", 4))));

        // Load item fill slots
        if (config.contains("item-slots")) {
            queueConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slots")));
        } else if (config.contains("item-slot")) {
            queueConfig.getItemSlots().addAll(GuiItemConfig.parseSlots(config.get("item-slot")));
        }

        // Load decorations
        final ConfigurationSection decSec = config.getConfigurationSection("decorations");
        if (decSec != null) {
            for (final String key : decSec.getKeys(false)) {
                final ConfigurationSection section = decSec.getConfigurationSection(key);
                if (section != null) {
                    queueConfig.getDecorations().put(key, GuiDecoration.parse(key, section));
                }
            }
        }

        // Load buttons
        final ConfigurationSection btnSec = config.getConfigurationSection("buttons");
        if (btnSec != null) {
            if (btnSec.isConfigurationSection("previous-page")) {
                queueConfig.setPreviousPageButton(GuiItemConfig.parse(btnSec.getConfigurationSection("previous-page")));
            }
            if (btnSec.isConfigurationSection("next-page")) {
                queueConfig.setNextPageButton(GuiItemConfig.parse(btnSec.getConfigurationSection("next-page")));
            }
            if (btnSec.isConfigurationSection("empty")) {
                queueConfig.setEmptyButton(GuiItemConfig.parse(btnSec.getConfigurationSection("empty")));
            }
            if (btnSec.isConfigurationSection("queue")) {
                queueConfig.setQueueButton(GuiItemConfig.parse(btnSec.getConfigurationSection("queue")));
            }

            for (final String key : btnSec.getKeys(false)) {
                if (!key.equals("previous-page") && !key.equals("next-page")
                        && !key.equals("empty") && !key.equals("queue")) {
                    final ConfigurationSection section = btnSec.getConfigurationSection(key);
                    if (section != null) {
                        queueConfig.getExtraButtons().put(key, GuiItemConfig.parse(section));
                    }
                }
            }
        }

        return queueConfig;
    }
}
