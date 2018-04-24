package me.realized.duels.util.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public abstract class AbstractGui implements Updatable {

    private final Map<Inventory, Map<Integer, Button>> buttons = new HashMap<>();

    public abstract void open(final Player player);

    public abstract boolean isPart(final Inventory inventory);

    public abstract void on(final Player player, final Inventory top, final InventoryClickEvent event);

    public void on(final Player player, final Set<Integer> rawSlots, final InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public Optional<Button> get(final Inventory inventory, final int slot) {
        final Map<Integer, Button> buttons;
        return (buttons = this.buttons.get(inventory)) != null ? Optional.ofNullable(buttons.get(slot)) : Optional.empty();
    }

    public void set(final Inventory inventory, final int slot, final Button button) {
        buttons.computeIfAbsent(inventory, result -> new HashMap<>()).put(slot, button);
        inventory.setItem(slot, button.getDisplayed());
    }

    public void set(final Inventory inventory, final int from, final int to, final int height, final Button button) {
        Slots.fill(from, to, height, slot -> set(inventory, slot, button));
    }

    public void set(final Inventory inventory, final int from, final int to, final Button button) {
        Slots.fill(from, to, slot -> set(inventory, slot, button));
    }

    @Override
    public void update(final Player player) {
        buttons.forEach((inventory, data) -> data.forEach((slot, button) -> {
            button.update(player);
            inventory.setItem(slot, button.getDisplayed());
        }));
    }
}
