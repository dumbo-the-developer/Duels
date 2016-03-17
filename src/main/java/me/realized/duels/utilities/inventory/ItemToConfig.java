package me.realized.duels.utilities.inventory;

import me.realized.duels.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.Map;

public class ItemToConfig {

    private ItemStack item;

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("type:").append(item.getType()).append("-").append("data:").append(item.getDurability()).append("-").append("amount:").append(item.getAmount()).append("-");

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                builder.append("name:").append(meta.getDisplayName()).append("-");
            }

            if (meta.hasLore()) {
                builder.append("lore:").append(StringUtil.join(meta.getLore(), ";")).append("-");
            }

            if (!Bukkit.getBukkitVersion().equals("1.7.10-R0.1-SNAPSHOT")) {
                if (!meta.getItemFlags().isEmpty()) {
                    builder.append("flags:");

                    for (ItemFlag flag : meta.getItemFlags()) {
                        builder.append(flag.name()).append(";");
                    }

                    builder.append("-");
                }
            }

            if (Bukkit.getBukkitVersion().equals("1.9-R0.1-SNAPSHOT") && item.getType() == Material.TIPPED_ARROW) {
                TippedArrow arrow = (TippedArrow) item;
                if (arrow.hasCustomEffects()) {
                    for (PotionEffect effect : arrow.getCustomEffects()) {
                        builder.append("a_").append(effect.getType()).append(":").append(effect.getDuration()).append(";").append(effect.getAmplifier()).append("-");
                    }
                }
            }

            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                builder.append(entry.getKey().getName()).append(":").append(entry.getValue()).append("-");
            }

            if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3) {
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

                if (skullMeta.hasOwner()) {
                    builder.append("owner:").append(skullMeta.getOwner()).append("-");
                }
            }

            if (item.getType() == Material.WRITTEN_BOOK) {
                BookMeta bookMeta = (BookMeta) item.getItemMeta();

                if (bookMeta.hasAuthor()) {
                    builder.append("author:").append(bookMeta.getAuthor()).append("-");
                }

                if (bookMeta.hasTitle()) {
                    builder.append("title:").append(bookMeta.getTitle()).append("-");
                }

                if (bookMeta.hasPages()) {
                    builder.append("pages:").append(StringUtil.join(bookMeta.getPages(), ";")).append("-");
                }
            }

            if (item.getType() == Material.POTION) {
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

                if (potionMeta.hasCustomEffects()) {
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        builder.append(effect.getType()).append(":").append(effect.getDuration()).append(";").append(effect.getAmplifier()).append("-");
                    }
                }
            }

            if (item.getType().name().contains("LEATHER_")) {
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
                builder.append("color:").append(DyeColor.getByColor(leatherArmorMeta.getColor())).append("-");
            }
        }

        return builder.toString();
    }
}
