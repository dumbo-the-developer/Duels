package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.extra.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ItemBettingButton extends BaseButton {

    public ItemBettingButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND).name(plugin.getLang().getMessage("GUI.settings.buttons.item-betting.name")).build());
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

        final Settings settings = settingManager.getSafely(player);
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.item-betting.lore",
            "item_betting", settings.isItemBetting() ? "&aenabled" : "&cdisabled");
        setLore(lore.split("\n"));
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

        final Settings settings = settingManager.getSafely(player);
        settings.setItemBetting(!settings.isItemBetting());
        settings.updateGui(player);
    }
}
