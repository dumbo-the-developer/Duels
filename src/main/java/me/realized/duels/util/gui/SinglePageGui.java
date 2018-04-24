package me.realized.duels.util.gui;

import java.util.Optional;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.inventory.InventoryBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SinglePageGui extends AbstractGui {

    protected final Inventory inventory;

    public SinglePageGui(final String title, final int rows) {
        this.inventory = InventoryBuilder.of(StringUtil.color(title), rows * 9).build();
    }

    protected void set(final int slot, final Button button) {
        set(inventory, slot, button);
    }

    protected void set(final int from, final int to, final int height, final Button button) {
        set(inventory, from, to, height, button);
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

        final Optional<Button> cached = get(inventory, event.getSlot());

        if (!cached.isPresent()) {
            return;
        }

        cached.get().onClick(player);
    }
}
