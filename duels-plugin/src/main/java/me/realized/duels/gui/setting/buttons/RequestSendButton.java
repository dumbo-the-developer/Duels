package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.TextBuilder;
import me.realized.duels.util.inventory.ItemBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestSendButton extends BaseButton {

    public RequestSendButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name(plugin.getLang().getMessage("GUI.settings.buttons.send.name")).build());
    }

    @Override
    public void onClick(final Player player) {
        final Settings settings = settingManager.getSafely(player);

        if (settings.getTarget() == null) {
            settings.reset();
            player.closeInventory();
            return;
        }

        final Player target = Bukkit.getPlayer(settings.getTarget());

        if (target == null) {
            settings.reset();
            player.closeInventory();
            lang.sendMessage(player, "ERROR.player.no-longer-online");
            return;
        }

        if (!config.isUseOwnInventoryEnabled() && settings.getKit() == null) {
            player.closeInventory();
            return;
        }

        player.closeInventory();
        settings.getLocations()[0] = player.getLocation().clone();

        if (!requestManager.send(player, target, settings)) {
            return;
        }

        final String kit = settings.getKit() != null ? settings.getKit().getName() : "Not Selected";
        final String arena = settings.getArena() != null ? settings.getArena().getName() : "Random";
        final int betAmount = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(player, "COMMAND.duel.request.send.sender",
            "name", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.send.receiver",
            "name", player.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        final String path = "COMMAND.duel.request.send.clickable-text.";

        TextBuilder
            .of(lang.getMessage(path + "info"))
            .add(lang.getMessage(path + "accept"), Action.RUN_COMMAND, "/duel accept " + player.getName())
            .add(lang.getMessage(path + "deny"), Action.RUN_COMMAND, "/duel deny " + player.getName())
            .send(target);
        TextBuilder.of(lang.getMessage(path + "extra")).send(target);
    }
}
