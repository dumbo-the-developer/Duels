package com.meteordevelopments.duels.gui.inventory.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.stream.Collectors;

public class EffectsButton extends BaseButton {

    public EffectsButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder
                .of(Items.WATER_BREATHING_POTION.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.effects.name"), plugin.getLang())
                .lore(player.getActivePotionEffects().stream()
                        .map(effect -> plugin.getLang().getMessage("GUI.inventory-view.buttons.effects.lore-format",
                                "type", StringUtil.capitalize(effect.getType().getName().replace("_", " ").toLowerCase()),
                                "amplifier", StringUtil.toRoman(effect.getAmplifier() + 1),
                                "duration", (effect.getDuration() / 20))).collect(Collectors.toList()), plugin.getLang())
                .build());
        editMeta(meta -> {
            if (CompatUtil.hasItemFlag()) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
        });
    }
}
