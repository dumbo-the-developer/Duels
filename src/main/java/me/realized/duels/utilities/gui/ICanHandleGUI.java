package me.realized.duels.utilities.gui;

import org.bukkit.inventory.ItemStack;

public interface ICanHandleGUI {

    ItemStack toDisplay();

    boolean filter();

}
