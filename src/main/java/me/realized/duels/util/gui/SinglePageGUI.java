package me.realized.duels.util.gui;

import java.util.Optional;
import me.realized.duels.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SinglePageGUI extends AbstractGUI {

    private final Inventory inventory;

    public SinglePageGUI(final AbstractGUI parent, final String title, final int rows) {
        super(parent);
        this.inventory = Bukkit.createInventory(null, rows * 9, StringUtil.color(title));
    }

    public SinglePageGUI(final String title, final int rows) {
        this(null, title, rows);
    }

    public void add(final int slot, final Button button) {
        add(inventory, slot, button);
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void open(final Player player) {
        update(player);
        player.openInventory(inventory);
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return inventory.equals(this.inventory);
    }

    @Override
    public void on(final Player player, final Inventory top, final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final Optional<Button> cached = of(inventory, event.getSlot());

        if(cached.isPresent()) {
            cached.get();
        }
    }
}
