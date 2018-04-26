package me.realized.duels.util.gui;

import java.util.Arrays;
import java.util.function.Consumer;
import lombok.Getter;
import me.realized.duels.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Button implements Updatable {

    @Getter
    private final ItemStack displayed;

    public Button(final ItemStack displayed) {
        this.displayed = displayed;
    }

    private void editMeta(final Consumer<ItemMeta> consumer) {
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

    @Override
    public void update(final Player player) {}

    public void onClick(final Player player) {}
}
