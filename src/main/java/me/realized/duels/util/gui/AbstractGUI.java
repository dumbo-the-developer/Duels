package me.realized.duels.util.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class AbstractGUI implements Updatable {

    @Getter
    private final AbstractGUI parent;
    private final Map<Inventory, Map<Integer, Button>> buttons = new HashMap<>();

    public AbstractGUI(final AbstractGUI parent) {
        this.parent = parent;
    }

    public AbstractGUI() {
        this(null);
    }

    public abstract void open(final Player player);

    public abstract boolean isPart(final Inventory inventory);

    public abstract void on(final Player player, final Inventory top, final InventoryClickEvent event);

    public Optional<Button> of(final Inventory inventory, final int slot) {
        final Map<Integer, Button> buttons;
        return (buttons = this.buttons.get(inventory)) != null ? Optional.ofNullable(buttons.get(slot)) : Optional.empty();
    }

    public void add(final Inventory inventory, final int slot, final Button button) {
        buttons.computeIfAbsent(inventory, result -> new HashMap<>()).put(slot, button);
        inventory.setItem(slot, button.getDisplayed());
    }

    public void openParent(final Player player) {
        if (parent != null) {
            parent.open(player);
        }
    }

    @Override
    public void update(final Player player) {
        buttons.forEach((inventory, data) -> data.forEach((slot, button) -> {
            button.update(player);
            inventory.setItem(slot, button.getDisplayed());
        }));
    }
}
