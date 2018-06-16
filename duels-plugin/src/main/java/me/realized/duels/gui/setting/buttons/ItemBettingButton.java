package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.extra.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ItemBettingButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Bet Items: %s";

    public ItemBettingButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND).name("&eAllow Betting Items").build());
    }

    @Override
    public void update(final Player player) {
        if (!config.isItemBettingEnabled()) {
            setLore("&cThis option is currently unavailable.");
            return;
        }

        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING)) {
            setLore("&cYou do not have permission to use this option.");
            return;
        }

        final Setting setting = settingManager.getSafely(player);
        setLore(String.format(LORE_TEMPLATE, setting.isItemBetting() ? "&aenabled" : "&cdisabled"));
    }

    @Override
    public void onClick(final Player player) {
        if (!config.isItemBettingEnabled()) {
            player.sendMessage(ChatColor.RED + "This option is currently unavailable.");
            return;
        }

        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ITEM_BETTING);
            return;
        }

        final Setting setting = settingManager.getSafely(player);
        setting.setItemBetting(!setting.isItemBetting());
        setting.updateGui(player);
    }
}
