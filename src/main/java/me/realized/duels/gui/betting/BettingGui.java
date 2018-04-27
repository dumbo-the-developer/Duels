package me.realized.duels.gui.betting;

import java.util.Set;
import java.util.UUID;
import me.realized.duels.util.gui.AbstractGui;
import me.realized.duels.util.inventory.InventoryBuilder;
import me.realized.duels.util.inventory.ItemBuilder;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class BettingGui extends AbstractGui {

    private class Section {

        private final int start, end, height;

        Section(final int start, final int end, final int height) {
            this.start = start;
            this.end = end;
            this.height = height;
        }

        private boolean isPart(final int slot) {
            for (int y = 0; y < height; y++) {
                for (int x = start; x < end; x++) {
                    if (x + y * 9 == slot) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private Section[] sections = {
        new Section(9, 13, 4),
        new Section(14, 18, 4)
    };

    private final UUID first, second;
    private final Inventory inventory;

    public BettingGui(final Player first, final Player second) {
        this.inventory = InventoryBuilder.of("Winner Takes All!", 54).build();
        this.first = first.getUniqueId();
        this.second = second.getUniqueId();
        Slots.fill(13, 14, 5, slot -> inventory.setItem(slot, ItemBuilder.of(Material.IRON_FENCE).name(" ").build()));
        Slots.fill(0, 3, slot -> inventory.setItem(slot, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 1).name(" ").build()));
        Slots.fill(45, 48, slot -> inventory.setItem(slot, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 1).name(" ").build()));
        Slots.fill(6, 9, slot -> inventory.setItem(slot, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 11).name(" ").build()));
        Slots.fill(51, 54, slot -> inventory.setItem(slot, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 11).name(" ").build()));
        inventory.setItem(48, ItemBuilder.of(Material.SKULL_ITEM, 1, (short) 3).head(first).build());
        inventory.setItem(50, ItemBuilder.of(Material.SKULL_ITEM, 1, (short) 3).head(second).build());
        inventory.setItem(3, ItemBuilder.of(Material.INK_SACK, 1, (short) 8).name("&7&lNOT READY").build());
        inventory.setItem(5, ItemBuilder.of(Material.INK_SACK, 1, (short) 10).name("&a&lREADY").build());
        inventory.setItem(4, ItemBuilder.of(Material.SIGN).name("&eMatch Details").build());
    }

    private Section getSection(final Player player) {
        return player.getUniqueId().equals(first) ? sections[0] : (player.getUniqueId().equals(second) ? sections[1] : null);
    }

    @Override
    public void open(final Player player) {
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

        final int slot = event.getSlot();

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }

        if (!clicked.equals(top)) {
            return;
        }

        final Section section = getSection(player);

        if (section == null) {
            return;
        }

        if (!section.isPart(slot)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void on(final Player player, final Set<Integer> rawSlots, final InventoryDragEvent event) {
        final Section section = getSection(player);

        if (section == null) {
            return;
        }

        boolean in = false;
        boolean out = false;
        boolean outSec = false;

        for (final int slot : rawSlots) {
            if (slot > 53) {
                out = true;
            } else {
                if (!section.isPart(slot)) {
                    outSec = true;
                }

                in = true;
            }
        }

        if (in && (out || outSec)) {
            event.setCancelled(true);
        }
    }
}
