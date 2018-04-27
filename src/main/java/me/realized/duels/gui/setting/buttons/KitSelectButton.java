package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Selected Kit: &9%s";

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND_SWORD).name("&eKit Selection").build());
    }

    @Override
    public void update(final Player player) {
        final Setting setting = settingCache.get(player);
        setLore(String.format(LORE_TEMPLATE, setting.getKit() != null ? setting.getKit().getName() : "Random"));
    }

    @Override
    public void onClick(final Player player) {
        // if kit selection is disabled in config, send msg
        kitManager.getGui().open(player);
    }
}
