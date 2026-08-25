package com.meteordevelopments.duels.util.gui;

import com.google.common.collect.Lists;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.configuration.GuiDecoration;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Inventories;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.InventoryBuilder;
import com.meteordevelopments.duels.util.inventory.Slots;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public class MultiPageGui<P extends JavaPlugin> extends AbstractGui<P> {

    private final String title;
    private final int size;
    private int prevPageSlot, nextPageSlot;

    @Getter
    private final Collection<? extends Button<P>> buttons;

    private PageNode first;

    @Setter
    private ItemStack spaceFiller;
    @Setter
    private ItemStack prevButton;
    @Setter
    private ItemStack nextButton;
    @Setter
    private ItemStack emptyIndicator;

    // Enhanced configurability
    @Getter
    @Setter
    private List<Integer> itemSlots = new ArrayList<>();
    @Getter
    @Setter
    private List<Integer> prevButtonSlots = new ArrayList<>();
    @Getter
    @Setter
    private List<Integer> nextButtonSlots = new ArrayList<>();
    @Getter
    @Setter
    private List<Integer> emptyIndicatorSlots = new ArrayList<>();
    @Getter
    @Setter
    private Map<String, GuiDecoration> decorations = new LinkedHashMap<>();
    @Getter
    @Setter
    private Button<P> backButton;
    @Getter
    @Setter
    private List<Integer> backButtonSlots = new ArrayList<>();

    public MultiPageGui(final P plugin, final String title, final int rows, final Collection<? extends Button<P>> buttons) {
        super(plugin);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }

        this.title = title;

        if (rows <= 0 || rows > 6) {
            throw new IllegalArgumentException("rows out of range, must be between 1 - 6");
        }

        this.size = rows * 9;
        this.prevPageSlot = size - 9;
        this.nextPageSlot = size - 1;

        if (buttons == null) {
            throw new IllegalArgumentException("buttons cannot be null");
        }

        this.buttons = buttons;
    }

    public void setPrevButtonSlots(final List<Integer> slots, final ItemStack item) {
        this.prevButtonSlots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        this.prevButton = item;
        if (!prevButtonSlots.isEmpty()) {
            this.prevPageSlot = prevButtonSlots.get(0);
        }
    }

    public void setNextButtonSlots(final List<Integer> slots, final ItemStack item) {
        this.nextButtonSlots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        this.nextButton = item;
        if (!nextButtonSlots.isEmpty()) {
            this.nextPageSlot = nextButtonSlots.get(0);
        }
    }

    public void setEmptyIndicatorSlots(final List<Integer> slots, final ItemStack item) {
        this.emptyIndicatorSlots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        this.emptyIndicator = item;
    }

    public void setBackButton(final List<Integer> slots, final Button<P> backButton) {
        this.backButtonSlots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
        this.backButton = backButton;
    }

    private String formatTitle(final int page, final int total) {
        final String combined = title + " (" + page + "/" + total + ")";
        if (plugin instanceof DuelsPlugin) {
            return ((DuelsPlugin) plugin).getLang().toLegacyString(combined);
        }
        return StringUtil.color(combined);
    }

    private String formatTitle(final String rawTitle) {
        if (plugin instanceof DuelsPlugin) {
            return ((DuelsPlugin) plugin).getLang().toLegacyString(rawTitle);
        }
        return StringUtil.color(rawTitle);
    }

    /**
     * Recalculates the pages for this {@link MultiPageGui}.
     */
    public void calculatePages() {
        final boolean customSlots = itemSlots != null && !itemSlots.isEmpty();
        final int maxSize = customSlots ? itemSlots.size() : Math.max(1, size - 9);
        final int totalPages = buttons.size() / maxSize + (buttons.size() % maxSize > 0 ? 1 : 0);

        if (first == null) {
            first = createPage(1, totalPages);
        }

        if (totalPages == 0) {
            first.setEmpty();
            return;
        }

        int i = 0;
        int pageNum = 1;
        PageNode last = null;

        for (final Button<P> button : buttons) {
            if (i % maxSize == 0) {
                final PageNode prev = last;

                if (last == null) {
                    last = first;
                } else {
                    if (last.next == null) {
                        last.next = createPage(pageNum, totalPages);
                    }
                    last = last.next;
                }

                last.setTitle(formatTitle(pageNum, totalPages));
                last.clear();
                applyDecorations(last.inventory);

                if (prev != null) {
                    last.previous = prev;
                    setPrevButtons(last.inventory);
                    prev.next = last;
                    setNextButtons(prev.inventory);
                }

                applyBackButton(last);

                pageNum++;
            }

            final int slot = customSlots ? itemSlots.get(i % maxSize) : (i % maxSize);
            set(last.inventory, slot, button);
            i++;
        }

        if (last != null) {
            last.resetNext();
        }
    }

    private void applyDecorations(final Inventory inventory) {
        if (decorations != null && !decorations.isEmpty()) {
            for (final GuiDecoration decoration : decorations.values()) {
                final GuiItemConfig itemConfig = decoration.getItemConfig();
                for (final int slot : decoration.getSlots()) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        final boolean glow = itemConfig.isGlowingAt(slot);
                        final ItemStack item = itemConfig.buildItem(plugin instanceof DuelsPlugin ? ((DuelsPlugin) plugin).getLang() : null, glow);
                        inventory.setItem(slot, item);
                    }
                }
            }
        }
    }

    private void applyBackButton(final PageNode node) {
        if (backButton != null && backButtonSlots != null) {
            for (final int slot : backButtonSlots) {
                if (slot >= 0 && slot < node.inventory.getSize()) {
                    set(node.inventory, slot, backButton);
                }
            }
        }
    }

    private void setPrevButtons(final Inventory inventory) {
        if (prevButton != null) {
            if (prevButtonSlots != null && !prevButtonSlots.isEmpty()) {
                for (final int slot : prevButtonSlots) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        inventory.setItem(slot, prevButton);
                    }
                }
            } else if (prevPageSlot >= 0 && prevPageSlot < inventory.getSize()) {
                inventory.setItem(prevPageSlot, prevButton);
            }
        }
    }

    private void setNextButtons(final Inventory inventory) {
        if (nextButton != null) {
            if (nextButtonSlots != null && !nextButtonSlots.isEmpty()) {
                for (final int slot : nextButtonSlots) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        inventory.setItem(slot, nextButton);
                    }
                }
            } else if (nextPageSlot >= 0 && nextPageSlot < inventory.getSize()) {
                inventory.setItem(nextPageSlot, nextButton);
            }
        }
    }

    private PageNode createPage(final int page, final int total) {
        final String pageTitle = formatTitle(page, total);
        final Inventory inv;
        if (decorations != null && !decorations.isEmpty()) {
            inv = InventoryBuilder.of(pageTitle, size).build();
            applyDecorations(inv);
        } else {
            inv = InventoryBuilder
                    .of(pageTitle, size)
                    .fillRange(prevPageSlot, nextPageSlot + 1, getSpaceFiller())
                    .build();
        }

        final PageNode node = new PageNode(inv);
        applyBackButton(node);
        return node;
    }

    private ItemStack getSpaceFiller() {
        return spaceFiller != null ? spaceFiller : Items.WHITE_PANE.clone();
    }

    @Override
    public void open(final Player... players) {
        for (final Player player : players) {
            update(player);
            if (first != null && first.inventory != null) {
                player.openInventory(first.inventory);
            }
        }
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return first != null && first.isPart(inventory);
    }

    @Override
    public void on(final Player player, final Inventory top, final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final PageNode node = first.find(clicked);

        if (node == null) {
            return;
        }

        final int slot = event.getSlot();

        final boolean isNext = (nextButtonSlots != null && nextButtonSlots.contains(slot)) || slot == nextPageSlot;
        final boolean isPrev = (prevButtonSlots != null && prevButtonSlots.contains(slot)) || slot == prevPageSlot;

        if (isNext && node.next != null) {
            player.openInventory(node.next.inventory);
        } else if (isPrev && node.previous != null) {
            player.openInventory(node.previous.inventory);
        } else {
            final Button<P> button = get(clicked, slot);

            if (button == null) {
                return;
            }

            button.onClick(player, event);
        }
    }

    private class PageNode {

        private final Inventory inventory;
        private PageNode previous, next;

        PageNode(final Inventory inventory) {
            this.inventory = inventory;
        }

        void setEmpty() {
            setTitle(formatTitle(title));

            clear();
            applyDecorations(inventory);
            applyBackButton(this);

            if (emptyIndicator != null) {
                if (emptyIndicatorSlots != null && !emptyIndicatorSlots.isEmpty()) {
                    for (final int slot : emptyIndicatorSlots) {
                        if (slot >= 0 && slot < inventory.getSize()) {
                            inventory.setItem(slot, emptyIndicator);
                        }
                    }
                } else if (inventory.getSize() > 4) {
                    inventory.setItem(4, emptyIndicator);
                }
            }

            resetNext();
        }

        void resetNext() {
            if (next == null) {
                return;
            }

            if (nextButtonSlots != null && !nextButtonSlots.isEmpty()) {
                for (final int slot : nextButtonSlots) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        inventory.setItem(slot, null);
                    }
                }
            } else if (nextPageSlot >= 0 && nextPageSlot < inventory.getSize()) {
                inventory.setItem(nextPageSlot, getSpaceFiller());
            }

            applyDecorations(inventory);

            forEach(node -> {
                if (node.equals(this)) {
                    return;
                }

                remove(node.inventory);
                Lists.newArrayList(node.inventory.getViewers()).forEach(HumanEntity::closeInventory);
            });
            next = null;
        }

        void setTitle(final String title) {
            Inventories.setTitle(inventory, formatTitle(title));
        }

        void clear() {
            remove(inventory);

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                inventory.setItem(slot, null);
            }
        }

        void resetBottom() {
            Slots.run(prevPageSlot, nextPageSlot + 1, slot -> inventory.setItem(slot, getSpaceFiller()));
        }

        void forEach(final Consumer<PageNode> consumer) {
            consumer.accept(this);

            if (next != null) {
                next.forEach(consumer);
            }
        }

        PageNode find(final Inventory inventory) {
            if (this.inventory.equals(inventory)) {
                return this;
            }

            if (next != null) {
                return next.find(inventory);
            }

            return null;
        }

        boolean isPart(final Inventory inventory) {
            return find(inventory) != null;
        }
    }
}
