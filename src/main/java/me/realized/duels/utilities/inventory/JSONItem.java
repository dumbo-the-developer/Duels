package me.realized.duels.utilities.inventory;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONItem {

    private final String material;
    private final int amount;
    private final short data;

    private Map<String, Integer> enchantments;
    private String displayName;
    private List<String> lore;
    private List<String> flags;

    private String owner;

    private String author;
    private String title;
    private List<String> contents;

    private Map<String, String> effects;

    private String color;

    public JSONItem(ItemStack item) {
        this.material = item.getType().name();
        this.amount = item.getAmount();
        this.data = item.getDurability();
    }

    private void addEnchantment(String type, int level) {
        if (enchantments == null) {
            enchantments = new HashMap<>();
        }

        enchantments.put(type, level);
    }

    private void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private void setLore(List<String> lore) {
        this.lore = lore;
    }

    private void addFlag(String flag) {
        if (flags == null) {
            flags = new ArrayList<>();
        }

        flags.add(flag);
    }

    private void setOwner(String owner) {
        this.owner = owner;
    }

    private void setAuthor(String author) {
        this.author = author;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setContents(List<String> contents) {
        this.contents = contents;
    }

    private void addEffect(String type, String data) {
        if (effects == null) {
            effects = new HashMap<>();
        }

        effects.put(type, data);
    }

    private void setColor(String color) {
        this.color = color;
    }

    public ItemStack construct() {
        ItemStack item = new ItemStack(Material.getMaterial(this.material), this.amount, this.data);

        if (enchantments != null && !enchantments.isEmpty()) {
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                item.addUnsafeEnchantment(Enchantment.getByName(entry.getKey()), entry.getValue());
            }
        }

        ItemMeta meta = item.getItemMeta();

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        if (!Bukkit.getBukkitVersion().equals("1.7.10-R0.1-SNAPSHOT") && flags != null && !flags.isEmpty()) {
            for (String flag : flags) {
                meta.addItemFlags(ItemFlag.valueOf(flag));
            }
        }

        item.setItemMeta(meta);

        if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3 && owner != null) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            skullMeta.setOwner(owner);
            item.setItemMeta(skullMeta);
        }

        if (item.getType() == Material.POTION && effects != null && !effects.isEmpty()) {
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

            for (Map.Entry<String, String> entry : effects.entrySet()) {
                int duration = Integer.valueOf(entry.getValue().split("-")[0]);
                int amplifier = Integer.valueOf(entry.getValue().split("-")[1]);
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(entry.getKey()), duration, amplifier), true);
            }

            item.setItemMeta(potionMeta);
        }

        if (item.getType() == Material.WRITTEN_BOOK) {
            BookMeta bookMeta = (BookMeta) item.getItemMeta();

            if (author != null) {
                bookMeta.setAuthor(author);
            }

            if (title != null) {
                bookMeta.setTitle(title);
            }

            if (contents != null && !contents.isEmpty()) {
                bookMeta.setPages(contents);
            }

            item.setItemMeta(bookMeta);
        }

        if (item.getType().name().contains("LEATHER_")) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();

            if (color != null) {
                leatherArmorMeta.setColor(DyeColor.valueOf(color).getColor());
            }

            item.setItemMeta(leatherArmorMeta);
        }

        return item;
    }

    public static JSONItem fromItemStack(ItemStack item) {
        if (item == null) {
            return null;
        }

        JSONItem result = new JSONItem(item);

        if (!item.getEnchantments().isEmpty()) {
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                result.addEnchantment(entry.getKey().getName(), entry.getValue());
            }
        }

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                result.setDisplayName(meta.getDisplayName());
            }

            if (meta.hasLore()) {
                result.setLore(meta.getLore());
            }

            if (!Bukkit.getBukkitVersion().equals("1.7.10-R0.1-SNAPSHOT") && !meta.getItemFlags().isEmpty()) {
                for (ItemFlag flag : meta.getItemFlags()) {
                    result.addFlag(flag.name());
                }
            }

            if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3) {
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

                if (skullMeta.hasOwner()) {
                    result.setOwner(skullMeta.getOwner());
                }
            }

            if (item.getType() == Material.POTION) {
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

                if (potionMeta.hasCustomEffects()) {
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        result.addEffect(effect.getType().getName(), effect.getDuration() + "-" + effect.getAmplifier());
                    }
                }
            }

            if (item.getType() == Material.WRITTEN_BOOK) {
                BookMeta bookMeta = (BookMeta) item.getItemMeta();

                if (bookMeta.hasAuthor()) {
                    result.setAuthor(bookMeta.getAuthor());
                }

                if (bookMeta.hasTitle()) {
                    result.setTitle(bookMeta.getTitle());
                }

                if (bookMeta.hasPages()) {
                    result.setContents(bookMeta.getPages());
                }
            }

            if (item.getType().name().contains("LEATHER_")) {
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
                result.setColor(DyeColor.getByColor(leatherArmorMeta.getColor()).name());
            }
        }

        return result;
    }
}
