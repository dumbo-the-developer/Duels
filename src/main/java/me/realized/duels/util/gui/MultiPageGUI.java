package me.realized.duels.util.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.realized.duels.util.inventory.GUIBuilder;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MultiPageGUI extends AbstractGUI {

    private class Page {

        private Page previous, next;
        private final Inventory inventory;

        private Page(final Inventory inventory) {
            this.inventory = inventory;
        }
    }

    private final String title;
    private final int size, prevPageSlot, nextPageSlot;
    private final Collection<? extends Button> buttons;
    private final List<Page> pages = new ArrayList<>();

    public MultiPageGUI(final String title, final int rows, final Collection<? extends Button> buttons) {
        super(null);
        this.title = title;
        this.size = rows * 9 + 9;
        this.prevPageSlot = size - 9;
        this.nextPageSlot = size - 1;
        this.buttons = buttons;
    }

    public void calculatePages() {
        pages.clear();

        final ItemStack prevPage = ItemBuilder.of(Material.PAPER).name("&aPrevious Page").build();
        final ItemStack nextPage = ItemBuilder.of(Material.PAPER).name("&aNext Page").build();
        final int maxSize = size - 9;
        final int pages = buttons.size() / maxSize + (buttons.size() % maxSize > 0 ? 1 : 0);

        if (pages == 0) {
            this.pages.add(new Page(
                GUIBuilder.of(title, 18)
                    .set(4, ItemBuilder.of(Material.REDSTONE_BLOCK).name("&cThis page is empty.").build())
                    .fillRange(9, 18, ItemBuilder.of(Material.STAINED_GLASS_PANE).build()).build()));
            return;
        }

        int i = 0;
        int pageNum = 1;
        int slot = 0;
        Page page = null;

        for (final Button button : buttons) {
            if (i % maxSize == 0) {
                final Page prev = page;
                page = new Page(GUIBuilder.of(title + " (" + pageNum + "/" + pages + ")", size).fillRange(prevPageSlot, nextPageSlot + 1, ItemBuilder.of(Material.STAINED_GLASS_PANE).build()).build());

                if (prev != null) {
                    page.inventory.setItem(prevPageSlot, prevPage);
                    page.previous = prev;
                    prev.inventory.setItem(nextPageSlot, nextPage);
                    prev.next = page;
                }

                this.pages.add(page);
                slot = 0;
                pageNum++;
            }

            add(page.inventory, slot, button);
            i++;
            slot++;
        }
    }

    @Override
    public void open(final Player player) {
        player.openInventory(pages.get(0).inventory);
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return pages.stream().anyMatch(page -> page.inventory.equals(inventory));
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

        final Optional<Page> result = pages.stream().filter(page -> page.inventory.equals(clicked)).findFirst();

        if (!result.isPresent()) {
            return;
        }

        final Page page = result.get();

        if (event.getSlot() == nextPageSlot && page.next != null) {
            player.openInventory(page.next.inventory);
        } else if (event.getSlot() == prevPageSlot && page.previous != null) {
            player.openInventory(page.previous.inventory);
        }
    }
}
