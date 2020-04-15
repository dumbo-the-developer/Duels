package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class ArenaSelectButton extends BaseButton {

    public ArenaSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.EMPTY_MAP).name(plugin.getLang().getMessage("GUI.settings.buttons.arena-selector.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.arena-selector.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final String lore = lang.getMessage("GUI.settings.buttons.arena-selector.lore", "arena", arena);
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isArenaSelectingUsePermission() && !player.hasPermission(Permissions.ARENA_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ARENA_SELECTING);
            return;
        }

        arenaManager.getGui().open(player);
    }
}
