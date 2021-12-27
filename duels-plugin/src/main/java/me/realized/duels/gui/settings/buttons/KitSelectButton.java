package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND_SWORD).name(plugin.getLang().getMessage("GUI.settings.buttons.kit-selector.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.kit-selector.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String lore = lang.getMessage("GUI.settings.buttons.kit-selector.lore", "kit", kit);
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isKitSelectingUsePermission() && !player.hasPermission(Permissions.KIT_SELECTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.KIT_SELECTING);
            return;
        }

        kitManager.getGui().open(player);
    }
}
