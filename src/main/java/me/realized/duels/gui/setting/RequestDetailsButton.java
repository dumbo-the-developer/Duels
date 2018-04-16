package me.realized.duels.gui.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestDetailsButton extends Button {

    private static final String[] LORE_TEMPLATE = {"&7Opponent: &f%s", "&7Kit: &9%s", "&7Arena: &9%s", "&7Bet: &6%s", "&7Bet Items: %s"};

    private final SettingCache cache;

    public RequestDetailsButton(final DuelsPlugin plugin) {
        super(ItemBuilder.of(Material.SIGN).name("&eRequest Details").build());
        this.cache = plugin.getSettingCache();
    }

    @Override
    public void update(final Player player) {
        final Setting setting = cache.get(player);
        final String[] lore = LORE_TEMPLATE.clone();
        lore[0] = String.format(lore[0], setting.getTargetName());
        lore[1] = String.format(lore[1], setting.getKit() != null ? setting.getKit().getName() : "Random");
        lore[2] = String.format(lore[2], setting.getArena() != null ? setting.getArena().getName() : "Random");
        lore[3] = String.format(lore[3], "$" + setting.getBet());
        lore[4] = String.format(lore[4], setting.isItemBetting() ? "&aenabled" : "&cdisabled");
        setLore(lore);
    }
}
