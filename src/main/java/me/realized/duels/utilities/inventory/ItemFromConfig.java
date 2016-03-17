package me.realized.duels.utilities.inventory;

import me.realized.duels.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ItemFromConfig {

    private static final Pattern PATTERN = Pattern.compile(":");
    private static final Map<String, Enchantment> enchantments = new HashMap<>();
    private static final Map<String, PotionEffectType> effects = new HashMap<>();

    static {
        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment != null) {
                enchantments.put(enchantment.getName(), enchantment);
            }
        }

        for (PotionEffectType type : PotionEffectType.values()) {
            if (type != null) {
                effects.put(type.getName(), type);
            }
        }
    }

    private Material type;
    private int amount;
    private short data;
    private String line;

    public void setLine(String line) {
        this.line = line;
    }

    private ItemStack buildWithoutMeta() {
        String[] sData = line.split("-");

        try {
            for (String s : sData) {
                String[] split = PATTERN.split(s, 2);

                switch (split[0]) {
                    case "type":
                        this.type = Material.valueOf(split[1]);
                        break;
                    case "amount":
                        this.amount = Integer.parseInt(split[1]);
                        break;
                    case "data":
                        this.data = Short.parseShort(split[1]);
                        break;
                }
            }

            return new ItemStack(type, amount, data);
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack buildWithMeta() {
        String[] sData = line.split("-");
        ItemStack item = buildWithoutMeta();

        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        MetaBuilder builder = new MetaBuilder(item);

        for (String s : sData) {
            String[] split = PATTERN.split(s, 2);
            String type = split[0];

            if (type.equals("name")) {
                meta.setDisplayName(StringUtil.color(split[1]));
                item.setItemMeta(meta);
                continue;
            }

            if (type.equals("lore")) {
                meta.setLore(Arrays.asList(StringUtil.color(split[1].split(";"))));
                item.setItemMeta(meta);
                continue;
            }

            if (type.equals("flags")) {
                for (String flag : split[1].split(";")) {
                    if (flag != null && !flag.isEmpty()) {
                        meta.addItemFlags(ItemFlag.valueOf(flag));
                        item.setItemMeta(meta);
                    }
                }

                continue;
            }

            if (enchantments.get(type) != null) {
                item.addUnsafeEnchantment(enchantments.get(type), Integer.parseInt(split[1]));
                continue;
            }

            if (effects.get(type) != null) {
                builder.addEffect(new PotionEffect(effects.get(type), Integer.parseInt(split[1].split(";")[0]), Integer.parseInt(split[1].split(";")[0])));
            }

            if (Bukkit.getBukkitVersion().equals("1.9-R0.1-SNAPSHOT") && item.getType() == Material.TIPPED_ARROW) {
                if (type.split("_").length != 0 && effects.get(type.split("_")[1]) != null) {
                    ((TippedArrow) item).addCustomEffect(new PotionEffect(effects.get(type), Integer.parseInt(split[1].split(";")[0]), Integer.parseInt(split[1].split(";")[0])), true);
                }
            }

            if (type.equals("owner")) {
                builder.setOwner(split[1]);
                continue;
            }

            if (type.equals("color")) {
                builder.setColor(split[1]);
                continue;
            }

            if (type.equals("author")) {
                builder.setAuthor(split[1]);
                continue;
            }

            if (type.equals("title")) {
                builder.setTitle(split[1]);
                continue;
            }

            if (type.equals("pages")) {
                builder.setPages(split[1].split(";"));
            }
        }

        return item;
    }
}
