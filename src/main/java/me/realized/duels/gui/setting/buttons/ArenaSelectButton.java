package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Selected Arena: &9%s";

    public ArenaSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name("&eArena Selection").build());
    }

    @Override
    public void update(final Player player) {
        if (!config.isAllowArenaSelecting()) {
            setLore("&cThis option is currently unavailable.");
            return;
        }

        final Setting setting = settingCache.getSafely(player);
        setLore(String.format(LORE_TEMPLATE, setting.getArena() != null ? setting.getArena().getName() : "Random"));
    }

    @Override
    public void onClick(final Player player) {
        if (!config.isAllowArenaSelecting()) {
            player.sendMessage(ChatColor.RED + "This option is currently unavailable.");
            return;
        }

        arenaManager.getGui().open(player);
    }
}
