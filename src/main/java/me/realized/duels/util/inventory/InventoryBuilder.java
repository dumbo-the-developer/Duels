package me.realized.duels.util.inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryBuilder {

    private final Inventory gui;

    private InventoryBuilder(final String title, final int size) {
        this.gui = Bukkit.createInventory(null, size, title);
    }

    public static InventoryBuilder of(final String title, final int size) {
        return new InventoryBuilder(title, size);
    }

    public InventoryBuilder set(final int slot, final ItemStack item) {
        gui.setItem(slot, item);
        return this;
    }

    public InventoryBuilder fill(final ItemStack item, final int... slots) {
        for (final int slot : slots) {
            gui.setItem(slot, item);
        }

        return this;
    }

    public InventoryBuilder fillRange(final int from, final int to, final ItemStack item) {
        for (int slot = from; slot < to; slot++) {
            gui.setItem(slot, item);
        }

        return this;
    }

    public InventoryBuilder fillEmpty(final ItemStack item) {
        for (int slot = 0; slot < gui.getSize(); slot++) {
            final ItemStack target = gui.getItem(slot);

            if (target != null && target.getType() != Material.AIR) {
                gui.setItem(slot, item);
            }
        }

        return this;
    }

    public InventoryBuilder pattern(final Pattern pattern) {
        pattern.apply(gui);
        return this;
    }

    public Inventory build() {
        return gui;
    }

    public static final class Pattern {

        private final List<String> rows;
        private final Map<Character, ItemStack> keys = new HashMap<>();

        private Pattern(final String... rows) {
            this.rows = Arrays.asList(rows);
        }

        public static Pattern of(final String... rows) {
            return new Pattern(rows);
        }

        public Pattern specify(final char key, final ItemStack item) {
            keys.put(key, item);
            return this;
        }

        private void apply(final Inventory inventory) {
            for (int row = 0; row < rows.size(); row++) {
                final String pattern = rows.get(row);

                for (int i = 0; i < pattern.length(); i++) {
                    if (i > 8) {
                        break;
                    }

                    inventory.setItem(row * 9 + i, keys.get(pattern.charAt(i)).clone());
                }
            }
        }
    }
}
