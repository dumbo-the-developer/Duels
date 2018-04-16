package me.realized.duels.gui.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.kit.KitManager;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends Button {

    private static final String LORE_TEMPLATE = "&7Selected Kit: &9%s";

    private final KitManager kitManager;
    private final SettingCache cache;

    public KitSelectButton(final DuelsPlugin plugin) {
        super(ItemBuilder.of(Material.DIAMOND_SWORD).name("&eKit Selection").build());
        this.kitManager = plugin.getKitManager();
        this.cache = plugin.getSettingCache();
    }

    @Override
    public void update(final Player player) {
        final Setting setting = cache.get(player);
        setLore(String.format(LORE_TEMPLATE, setting.getKit() != null ? setting.getKit().getName() : "Random"));
    }

    @Override
    public void onClick(final Player player) {
        // if kit selection is disabled in config, send msg
        kitManager.getGui().open(player);
    }
}
