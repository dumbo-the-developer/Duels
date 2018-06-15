package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public class PotionCounterButton extends BaseButton {

    public PotionCounterButton(final DuelsPlugin plugin, final int count) {
        super(plugin, ItemBuilder.of(Material.POTION, 1, (short) 16421).name("&d" + count + " Health Potions").build());
        editMeta(meta -> {
            if (!CompatUtil.isPre1_8()) {
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            }
        });
    }
}
