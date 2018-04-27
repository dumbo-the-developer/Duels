package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ItemBettingButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Bet Items: %s";

    public ItemBettingButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND).name("&eAllow Betting Items").build());
    }

    @Override
    public void update(final Player player) {
        final Setting setting = settingCache.get(player);
        setLore(String.format(LORE_TEMPLATE, setting.isItemBetting() ? "&aenabled" : "&cdisabled"));
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingCache.get(player);
        setting.setItemBetting(!setting.isItemBetting());
        setting.updateGui(player);
    }
}
