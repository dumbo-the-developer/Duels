package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestSendButton extends BaseButton {

    public RequestSendButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name("&a&lSEND REQUEST").build());
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingCache.getSafely(player);

        if (setting.getTarget() == null) {
            setting.reset();
            player.closeInventory();
            return;
        }

        final Player target = Bukkit.getPlayer(setting.getTarget());

        if (target == null) {
            setting.reset();
            player.closeInventory();
            lang.sendMessage(player, "ERROR.no-longer-online");
            return;
        }

        player.closeInventory();
        requestManager.send(player, target, setting);

        final String kit = setting.getKit() != null ? setting.getKit().getName() : "Random";
        final String arena = setting.getArena() != null ? setting.getArena().getName() : "Random";
        final double betAmount = setting.getBet();
        final String itemBetting = setting.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(player, "COMMAND.duel.request.sent.sender",
            "player", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.sent.receiver",
            "player", player.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
    }
}
