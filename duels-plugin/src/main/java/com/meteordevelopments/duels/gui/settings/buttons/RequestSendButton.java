package com.meteordevelopments.duels.gui.settings.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RequestSendButton extends BaseButton {

    private final GuiItemConfig itemConfig;
    private final boolean glow;

    public RequestSendButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig, final boolean glow) {
        super(plugin, itemConfig.buildItem(plugin.getLang(), glow));
        this.itemConfig = itemConfig;
        this.glow = glow;
    }

    public RequestSendButton(final DuelsPlugin plugin, final GuiItemConfig itemConfig) {
        this(plugin, itemConfig, itemConfig.isGlowing());
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

        if (!settings.isOwnInventory() && settings.getKit() == null && settings.getCustomKit() == null && settings.getCustomKitSnapshot() == null) {
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
