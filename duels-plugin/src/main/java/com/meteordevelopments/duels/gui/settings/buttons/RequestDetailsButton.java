package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RequestDetailsButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public RequestDetailsButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public RequestDetailsButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
    }

    @Override
    public void update(final Player player) {
        final Settings settings = settingManager.getSafely(player);
        final Player target = Bukkit.getPlayer(settings.getTarget());

        if (target == null) {
            settings.reset();
            player.closeInventory();
            lang.sendMessage(player, "ERROR.player.no-longer-online");
            return;
        }

        final String kit;
        if (settings.getCustomKit() != null) {
            kit = "[Custom] " + settings.getCustomKit().getName();
        } else if (settings.getKit() != null) {
            kit = settings.getKit().getName();
        } else if (settings.isOwnInventory()) {
            kit = lang.getMessage("GUI.settings.buttons.use-own-inventory.name");
        } else {
            kit = lang.getMessage("GENERAL.not-selected");
        }

        final Map<String, String> placeholders = new HashMap<>();
        placeholders.put("opponent", target.getName());
        placeholders.put("kit", kit);
        placeholders.put("own_inventory", settings.isOwnInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled"));
        placeholders.put("arena", settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random"));
        placeholders.put("item_betting", settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled"));
        placeholders.put("bet_amount", String.valueOf(settings.getBet()));

        if (itemConfig.getLore() != null && !itemConfig.getLore().isEmpty()) {
            final String[] lines = itemConfig.getLore().stream()
                    .map(line -> {
                        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
                            line = line.replace("%" + entry.getKey() + "%", entry.getValue());
                        }
                        return line;
                    })
                    .toArray(String[]::new);
            setLore(lang, lines);
        } else {
            final String lore = lang.getMessage("GUI.settings.buttons.details.lore",
                    "opponent", target.getName(),
                    "kit", kit,
                    "own_inventory", settings.isOwnInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled"),
                    "arena", settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random"),
                    "item_betting", settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled"),
                    "bet_amount", settings.getBet()
            );
            setLore(lang, lore.split("\n"));
        }
    }
}
