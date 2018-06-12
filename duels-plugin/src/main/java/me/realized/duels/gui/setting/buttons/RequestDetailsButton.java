package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestDetailsButton extends BaseButton {

    private static final String[] LORE_TEMPLATE = {"&7Opponent: &f%s", "&7Kit: &9%s", "&7Arena: &9%s", "&7Bet Items: %s", "&7Bet: &6%s", " ",
        "&7To change the bet", "&7amount, please type", "&a/duel %s [amount]"};

    public RequestDetailsButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.SIGN).name("&eRequest Details").build());
    }

    @Override
    public void update(final Player player) {
        final Setting setting = settingManager.getSafely(player);
        final Player target = Bukkit.getPlayer(setting.getTarget());

        if (target == null) {
            setting.reset();
            player.closeInventory();
            lang.sendMessage(player, "ERROR.no-longer-online");
            return;
        }

        final String[] lore = LORE_TEMPLATE.clone();
        lore[0] = String.format(lore[0], target.getName());
        lore[1] = String.format(lore[1], setting.getKit() != null ? setting.getKit().getName() : "Random");
        lore[2] = String.format(lore[2], setting.getArena() != null ? setting.getArena().getName() : "Random");
        lore[3] = String.format(lore[3], setting.isItemBetting() ? "&aenabled" : "&cdisabled");
        lore[4] = String.format(lore[4], "$" + setting.getBet());
        lore[8] = String.format(lore[8], target.getName());
        setLore(lore);
    }
}
