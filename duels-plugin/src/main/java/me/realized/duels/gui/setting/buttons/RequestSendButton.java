package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.TextBuilder;
import me.realized.duels.util.inventory.ItemBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestSendButton extends BaseButton {

    public RequestSendButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name("&a&lSEND REQUEST").build());
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingManager.getSafely(player);

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

        if (!config.isUseOwnInventoryEnabled() && setting.getKit() == null) {
            lang.sendMessage(player, "DUEL.no-selected-kit");
            return;
        }

        player.closeInventory();
        setting.getLocations()[0] = player.getLocation().clone();

        if (!requestManager.send(player, target, setting)) {
            return;
        }

        final String kit = setting.getKit() != null ? setting.getKit().getName() : "Not Selected";
        final String arena = setting.getArena() != null ? setting.getArena().getName() : "Random";
        final int betAmount = setting.getBet();
        final String itemBetting = setting.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(player, "COMMAND.duel.request.sent.sender",
            "player", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.sent.receiver",
            "player", player.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        final String path = "COMMAND.duel.request.sent.clickable-text.";

        TextBuilder
            .of(lang.getMessage(path + "info"))
            .add(lang.getMessage(path + "accept"), Action.RUN_COMMAND, "/duel accept " + player.getName())
            .add(lang.getMessage(path + "deny"), Action.RUN_COMMAND, "/duel deny " + player.getName())
            .send(target);
        TextBuilder.of(lang.getMessage(path + "extra")).send(target);
    }
}
