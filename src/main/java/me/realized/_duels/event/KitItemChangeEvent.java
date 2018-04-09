package me.realized._duels.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class KitItemChangeEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    private final ItemStack old;
    private ItemStack _new;

    public KitItemChangeEvent(String name, Player player, ItemStack old, ItemStack _new) {
        super(name, player);
        this.old = old;
        this._new = _new;
    }

    public ItemStack getOldItem() {
        return old;
    }

    public ItemStack getNewItem() {
        return _new;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
