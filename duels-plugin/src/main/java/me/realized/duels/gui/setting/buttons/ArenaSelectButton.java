package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.extra.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Selected Arena: &9%s";

    public ArenaSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name("&eArena Selection").build());
    }

    @Override
    public void update(final Player player) {
        if (!config.isArenaSelectingEnabled()) {
            setLore("&cThis option is currently unavailable.");
            return;
        }

        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING)) {
            setLore("&cYou do not have permission to use this option.");
            return;
        }

        final Setting setting = settingManager.getSafely(player);
        setLore(String.format(LORE_TEMPLATE, setting.getArena() != null ? setting.getArena().getName() : "Random"));
    }

    @Override
    public void onClick(final Player player) {
        if (!config.isArenaSelectingEnabled()) {
            player.sendMessage(ChatColor.RED + "This option is currently unavailable.");
            return;
        }

        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ARENA_SELECTING);
            return;
        }

        arenaManager.getGui().open(player);
    }
}
