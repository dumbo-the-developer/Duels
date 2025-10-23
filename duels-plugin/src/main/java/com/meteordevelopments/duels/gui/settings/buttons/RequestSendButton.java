package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RequestSendButton extends BaseButton {

    public RequestSendButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.GREEN_PANE.clone()).name(plugin.getLang().getMessage("GUI.settings.buttons.send.name"), plugin.getLang()).build());
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

        if (!settings.isOwnInventory() && settings.getKit() == null) {
            player.closeInventory();
            lang.sendMessage(player, "ERROR.duel.mode-unselected");
            return;
        }

        final Party senderParty = settings.getSenderParty();
        final Party targetParty = settings.getTargetParty();

        if ((senderParty != null && senderParty.isRemoved()) || (targetParty != null && targetParty.isRemoved())) {
            player.closeInventory();
            lang.sendMessage(player, "ERROR.party.not-found");
            return;
        }

        player.closeInventory();
        requestManager.send(player, target, settings);
    }
}
