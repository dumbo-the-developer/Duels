package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND_SWORD).name(plugin.getLang().getMessage("GUI.settings.buttons.kit-selector.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isUseOwnInventoryEnabled()) {
            setLore("&cThis option is currently", "&cunavailable. Your inventory", "&cwill be used instead", "&cin the duel.");
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.kit-selector.lore",
                "kit", settings.getKit() != null ? settings.getKit().getName() : "Not Selected");
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isUseOwnInventoryEnabled()) {
            player.sendMessage(ChatColor.RED + "This option is currently unavailable. Your inventory will be used instead in the duel.");
            return;
        }

        kitManager.getGui().open(player);
    }
}
