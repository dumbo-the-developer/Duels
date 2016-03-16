package me.realized.duels.utilities.inventory;

import me.realized.duels.utilities.StringUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;

    public ItemBuilder type(Material type) {
        item = new ItemStack(type);
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(StringUtil.color(name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        List<String> colored = new ArrayList<>();

        for (String s : lore) {
            colored.add(StringUtil.color(s));
        }

        ItemMeta meta = item.getItemMeta();
        meta.setLore(colored);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }

    public static ItemBuilder builder() {
        return new ItemBuilder();
    }
}
