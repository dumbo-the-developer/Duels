package com.meteordevelopments.duels.gui.kitedit.buttons;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.kit.edit.KitEditManager;
import org.bukkit.entity.Player;

/**
 * Button for selecting a kit to edit in the Kit Edit GUI.
 */
public class KitEditButton extends BaseButton {

    private final KitImpl kit;

    public KitEditButton(final DuelsPlugin plugin, final KitImpl kit) {
        super(plugin, kit.getDisplayed().clone());
        this.kit = kit;
    }

    @Override
    public void onClick(final Player player) {
        // Check if player is already editing
        if (KitEditManager.getInstance().isEditing(player)) {
            lang.sendMessage(player, "KIT.EDIT.already-editing");
            return;
        }
        
        // Check if player is in a queue
        if (queueManager.get(player) != null) {
            lang.sendMessage(player, "KIT.EDIT.in-queue");
            return;
        }
        
        // Check if player is in a match
        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "KIT.EDIT.in-match");
            return;
        }

        // Close the GUI
        player.closeInventory();

        // Start editing session
        if (KitEditManager.getInstance().startEditSession(player, kit.getName())) {
            lang.sendMessage(player, "KIT.EDIT.started", "kit", kit.getName());
        } else {
            lang.sendMessage(player, "KIT.EDIT.start-failed", "kit", kit.getName());
        }
    }
}
