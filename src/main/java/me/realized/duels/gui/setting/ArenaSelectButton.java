package me.realized.duels.gui.setting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.kit.KitManager;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends Button {

    private static final String LORE_TEMPLATE = "&7Selected Arena: &9%s";

    private final ArenaManager arenaManager;
    private final SettingCache cache;

    public ArenaSelectButton(final DuelsPlugin plugin) {
        super(ItemBuilder.of(Material.EMPTY_MAP).name("&eArena Selection").build());
        this.arenaManager = plugin.getArenaManager();
        this.cache = plugin.getSettingCache();
    }

    @Override
    public void update(final Player player) {
        final Setting setting = cache.get(player);
        setLore(String.format(LORE_TEMPLATE, setting.getArena() != null ? setting.getArena().getName() : "Random"));
    }

    @Override
    public void onClick(final Player player) {}
}
