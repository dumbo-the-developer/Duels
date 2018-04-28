package me.realized.duels.gui.betting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DetailsButton extends BaseButton {

    private static final String[] LORE_TEMPLATE = {"&7Kit: &9%s", "&7Arena: &9%s", "&7Bet: &6%s"};

    private final Setting setting;

    public DetailsButton(final DuelsPlugin plugin, final Setting setting) {
        super(plugin, ItemBuilder.of(Material.SIGN).name("&eMatch Details").build());
        this.setting = setting;
    }

    @Override
    public void update(final Player player) {
        final String[] lore = LORE_TEMPLATE.clone();
        lore[0] = String.format(lore[0], setting.getKit() != null ? setting.getKit().getName() : "Random");
        lore[1] = String.format(lore[1], setting.getArena() != null ? setting.getArena().getName() : "Random");
        lore[2] = String.format(lore[2], "$" + setting.getBet());
        setLore(lore);
    }
}
