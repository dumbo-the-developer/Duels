package me.realized.duels.gui;

import me.realized.duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GUI<T> {

    private final int BUTTON_PREVIOUS;
    private final int BUTTON_NEXT;

    private final String title;
    private final int rows;
    private final ClickListener listener;
    private final Map<Inventory, Integer> pages = new LinkedHashMap<>();
    private final Map<Integer, T> data = new LinkedHashMap<>();

    public GUI(String title, List<T> entries, int rows, ClickListener listener) {
        this.title = title;

        if (rows < 1 || rows > 5) {
            rows = 5;
        }

        this.BUTTON_PREVIOUS = rows * 9;
        this.BUTTON_NEXT = rows * 9 + 8;
        this.rows = rows;
        this.listener = listener;
        update(entries);
    }

    public void update(List<T> entries) {
        entries = new ArrayList<>(entries);
        Map<Inventory, Integer> previousPages = new HashMap<>(pages);
        pages.clear();
        data.clear();

        Iterator<T> iterator = entries.iterator();

        while (iterator.hasNext()) {
            T entry = iterator.next();

            if (entry instanceof ICanHandleGUI && !((ICanHandleGUI) entry).filter()) {
                iterator.remove();
            }
        }

        int maxInvSize = rows * 9;
        int total = entries.size() / maxInvSize + (entries.size() % maxInvSize > 0 ? 1 : 0);

        if (!entries.isEmpty()) {
            ItemStack next = ItemBuilder.builder().type(Material.PAPER).name("&aNext Page").build();
            ItemStack previous = ItemBuilder.builder().type(Material.PAPER).name("&aPrevious Page").build();
            ItemStack separator = ItemBuilder.builder().type(Material.STAINED_GLASS_PANE).name(" ").build();
            ItemStack defaultDisplayed = ItemBuilder.builder().type(Material.BARRIER).build();

            for (int page = 1; page <= total; page++) {
                Inventory current = Bukkit.createInventory(null, maxInvSize + 9, title + " (page " + page + "/" + total + ")");

                if (page < total) {
                    if (page != 1) {
                        current.setItem(BUTTON_PREVIOUS, previous);
                    }

                    current.setItem(BUTTON_NEXT, next);
                } else {
                    if (page != 1) {
                        current.setItem(BUTTON_PREVIOUS, previous);
                    }
                }

                for (int i = BUTTON_PREVIOUS; i <= BUTTON_NEXT; i++) {
                    if (current.getItem(i) == null) {
                        current.setItem(i, separator);
                    }
                }

                int end = page != total ? maxInvSize : (entries.size() % maxInvSize != 0 ? entries.size() % maxInvSize : maxInvSize);

                for (int start = 0; start < end; start++) {
                    T entry = entries.get(start + ((page - 1) * maxInvSize));
                    ItemStack displayed;

                    if (entry instanceof ICanHandleGUI) {
                        displayed = ((ICanHandleGUI) entry).toDisplay();
                        data.put(start + ((page - 1) * maxInvSize), entry);
                    } else {
                        displayed = defaultDisplayed;
                    }

                    current.setItem(start, displayed);
                }

                pages.put(current, page);
            }
        } else {
            Inventory current = Bukkit.createInventory(null, 54, title + " (page 1/1)");
            current.setItem(22, ItemBuilder.builder().type(Material.BARRIER).name(ChatColor.RED + "There was nothing to load to the GUI.").build());
            pages.put(current, 1);
        }

        for (Map.Entry<Inventory, Integer> entry : previousPages.entrySet()) {
            Iterator<HumanEntity> entityIterator = entry.getKey().getViewers().iterator();

            while (entityIterator.hasNext()) {
                HumanEntity entity = entityIterator.next();
                entityIterator.remove();

                if (entry.getValue() > total) {
                    entity.closeInventory();
                    entity.sendMessage(ChatColor.RED + "[Duels] The inventory you were looking was deleted.");
                } else {
                    for (Map.Entry<Inventory, Integer> newEntry : pages.entrySet()) {
                        if (newEntry.getValue().equals(entry.getValue())) {
                            listener.onSwitch((Player) entity, newEntry.getKey());
                            break;
                        }
                    }
                }
            }
        }
    }

    public Inventory getByPage(int page) {
        for (Map.Entry<Inventory, Integer> entry : pages.entrySet()) {
            if (entry.getValue() == page) {
                return entry.getKey();
            }
        }

        return null;
    }

    public boolean isPage(Inventory another) {
        for (Inventory inventory : pages.keySet()) {
            if (another.equals(inventory)) {
                return true;
            }
        }

        return false;
    }

    public T getData(Player player, Inventory inventory, int slot) {
        if (pages.get(inventory) == null) {
            return null;
        }

        int page = pages.get(inventory);

        if (slot >= BUTTON_PREVIOUS) {
            if (inventory.getItem(slot).getType() == Material.PAPER) {
                if (slot == BUTTON_NEXT) {
                    listener.onSwitch(player, getByPage(++page));
                } else if (slot == BUTTON_PREVIOUS) {
                    listener.onSwitch(player, getByPage(--page));
                }
            }

            return null;
        }

        return data.get((page - 1) * (rows * 9) + slot);
    }

    public void close(String warning) {
        for (Inventory inventory : pages.keySet()) {
            Iterator<HumanEntity> entityIterator = inventory.getViewers().iterator();

            while (entityIterator.hasNext()) {
                HumanEntity entity = entityIterator.next();
                entityIterator.remove();
                entity.closeInventory();

                if (warning != null) {
                    entity.sendMessage(ChatColor.RED + warning);
                }
            }
        }
    }

    public Inventory getFirst() {
        return pages.keySet().iterator().next();
    }

    protected ClickListener getListener() {
        return listener;
    }

    public interface ClickListener {

        void onClick(InventoryClickEvent event);

        void onClose(InventoryCloseEvent event);

        void onSwitch(Player player, Inventory opened);
    }
}
