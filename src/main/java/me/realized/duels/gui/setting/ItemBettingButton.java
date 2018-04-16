package me.realized.duels.gui.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ItemBettingButton extends Button {

    private static final String LORE_TEMPLATE = "&7Bet Items: %s";

    private final SettingCache cache;

    public ItemBettingButton(final DuelsPlugin plugin) {
        super(ItemBuilder.of(Material.DIAMOND).name("&eAllow Betting Items").build());
        this.cache = plugin.getSettingCache();
    }

    @Override
    public void update(final Player player) {
        final Setting setting = cache.get(player);
        setLore(String.format(LORE_TEMPLATE, setting.isItemBetting() ? "&aenabled" : "&cdisabled"));
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = cache.get(player);
        setting.setItemBetting(!setting.isItemBetting());
        setting.getGui().update(player);
    }
}
