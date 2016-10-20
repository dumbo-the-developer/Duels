package me.realized.duels.utilities.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public interface GUIListener {

    void onClick(InventoryClickEvent event);

    void onClose(InventoryCloseEvent event);

    void onSwitch(Player player, Inventory opened);
}
