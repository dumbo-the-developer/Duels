package me.realized.duels.utilities.inventory;

import me.realized.duels.utilities.StringUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

class MetaBuilder {

    private final ItemStack item;

    public MetaBuilder(ItemStack item) {
        this.item = item;
    }

    public void setOwner(String name) {
        if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(name);
            item.setItemMeta(meta);
        }
    }

    public void setAuthor(String author) {
        if (item.getType() == Material.WRITTEN_BOOK) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.setAuthor(author);
            item.setItemMeta(meta);
        }
    }

    public void setTitle(String title) {
        if (item.getType() == Material.WRITTEN_BOOK) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.setTitle(StringUtil.color(title));
            item.setItemMeta(meta);
        }
    }

    public void setPages(String[] pages) {
        if (item.getType() == Material.WRITTEN_BOOK) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.setPages(StringUtil.color(pages));
            item.setItemMeta(meta);
        }
    }

    public void setColor(String color) {
        if (item.getType().name().contains("LEATHER_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(DyeColor.valueOf(color).getColor());
            item.setItemMeta(meta);
        }
    }

    public void addEffect(PotionEffect effect) {
        if (item.getType() == Material.POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.addCustomEffect(effect, true);
            item.setItemMeta(meta);
        }
    }
}
