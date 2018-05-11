package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitSelectButton extends BaseButton {

    private static final String LORE_TEMPLATE = "&7Selected Kit: &9%s";

    public KitSelectButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND_SWORD).name("&eKit Selection").build());
    }

    @Override
    public void update(final Player player) {
        if (config.isUseOwnInventoryEnabled()) {
            setLore("&cThis option is currently", "&cunavailable. Your inventory", "&cwill be used instead", "&cin the duel.");
            return;
        }

        final Setting setting = settingCache.get(player);
        setLore(String.format(LORE_TEMPLATE, setting.getKit() != null ? setting.getKit().getName() : "Random"));
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
