package me.realized.duels.util.gui;

import java.util.Arrays;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    protected void setLore(final String... lore) {
        editMeta(meta -> meta.setLore(StringUtil.color(Arrays.asList(lore))));
    }

    protected void setOwner(final String name) {
        editMeta(meta -> {
            if (displayed.getType() == Material.SKULL_ITEM && displayed.getDurability() == 3) {
                ((SkullMeta) meta).setOwner(name);
            }
        });
    }

    public void update(final Player player) {}

    public void onClick(final Player player) {}
}
