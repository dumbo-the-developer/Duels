package me.realized.duels.gui.inventory.buttons;

import java.util.stream.Collectors;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EffectsButton extends BaseButton {

    public EffectsButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder.of(Material.POTION, 1, (short) 8237)
            .name("&bPotion Effects")
            .lore(player.getActivePotionEffects().stream()
                .map(effect -> StringUtil.color("&7"
                    + StringUtils.capitalize(effect.getType().getName().replace("_", " ").toLowerCase())
                    + " "
                    + StringUtil.toRoman(effect.getAmplifier() + 1)
                    + " (" + (effect.getDuration() / 20) + "s)")).collect(Collectors.toList())).build());
    }
}
