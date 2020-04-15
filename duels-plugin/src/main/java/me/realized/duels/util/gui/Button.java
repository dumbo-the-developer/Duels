package me.realized.duels.util.gui;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Items;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Button<P extends JavaPlugin> {

    protected final P plugin;
    @Getter
    @Setter
    private ItemStack displayed;

    public Button(final P plugin, final ItemStack displayed) {
        this.plugin = plugin;
        this.displayed = displayed;
    }

    protected void editMeta(final Consumer<ItemMeta> consumer) {
        final ItemMeta meta = getDisplayed().getItemMeta();
        consumer.accept(meta);
        getDisplayed().setItemMeta(meta);
    }

    protected void setDisplayName(final String name) {
        editMeta(meta -> meta.setDisplayName(StringUtil.color(name)));
    }

    protected void setLore(final List<String> lore) {
        editMeta(meta -> meta.setLore(StringUtil.color(lore)));
    }

    protected void setLore(final String... lore) {
        setLore(Arrays.asList(lore));
    }

    protected void setOwner(final String name) {
        editMeta(meta -> {
            if (Items.equals(Items.HEAD, displayed)) {
                ((SkullMeta) meta).setOwner(name);
            }
        });
    }

    protected void setGlow(final boolean glow) {
        editMeta(meta -> {
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, false);

                if (CompatUtil.hasItemFlag()) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            } else {
                meta.removeEnchant(Enchantment.DURABILITY);

                if (CompatUtil.hasItemFlag()) {
                    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
        });
    }


    public void update(final Player player) {}

    public void onClick(final Player player) {}
}
