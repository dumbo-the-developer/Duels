package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.extra.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends BaseButton {

    public ArenaSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.EMPTY_MAP).name(plugin.getLang().getMessage("GUI.settings.buttons.arena-selector.name")).build());
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

        final Settings settings = settingManager.getSafely(player);
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.arena-selector.lore",
            "arena", settings.getArena() != null ? settings.getArena().getName() : "Random");
        setLore(lore.split("\n"));
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
