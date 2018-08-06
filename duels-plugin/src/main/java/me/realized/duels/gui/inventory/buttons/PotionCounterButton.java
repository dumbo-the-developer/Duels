package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public class PotionCounterButton extends BaseButton {

    public PotionCounterButton(final DuelsPlugin plugin, final int count) {
        super(plugin, ItemBuilder
            .of(Material.POTION, 1, (short) 16421)
            .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.potion-counter.name", "potions", count))
            .build()
        );
        editMeta(meta -> {
            if (CompatUtil.hasItemFlag()) {
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            }
        });
    }
}
