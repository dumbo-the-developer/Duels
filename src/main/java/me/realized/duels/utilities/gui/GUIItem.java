package me.realized.duels.utilities.gui;

import org.bukkit.inventory.ItemStack;

public interface GUIItem {

    ItemStack toDisplay();

    boolean filter();

}
