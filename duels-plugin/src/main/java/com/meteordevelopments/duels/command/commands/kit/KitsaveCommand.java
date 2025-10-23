package com.meteordevelopments.duels.command.commands.kit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.kit.edit.KitEditManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to save the current kit and end editing mode.
 * Usage: /kitsave
 */
public class KitsaveCommand extends BaseCommand {
    
    public KitsaveCommand(DuelsPlugin plugin) {
        super(plugin, "kitsave", "kitsave", "Saves the current kit and ends editing mode.", 1, true);
    }
    
    public void executeCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check if player is editing
        if (!KitEditManager.getInstance().isEditing(player)) {
            lang.sendMessage(player, "KIT.EDIT.not-editing");
            return;
        }
        
        // Get current session info
        var session = KitEditManager.getInstance().getEditSession(player);
        String kitName = session.getKitName();
        
        // Save the kit
        if (KitEditManager.getInstance().saveKit(player)) {
            lang.sendMessage(player, "KIT.EDIT.saved", "kit", kitName);
        } else {
            lang.sendMessage(player, "KIT.EDIT.save-failed", "kit", kitName);
        }
    }
    
    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        // Check if player is editing
        if (!KitEditManager.getInstance().isEditing(player)) {
            lang.sendMessage(player, "KIT.EDIT.not-editing");
            return;
        }
        
        // Get current session info
        var session = KitEditManager.getInstance().getEditSession(player);
        String kitName = session.getKitName();
        
        // Save the kit
        if (KitEditManager.getInstance().saveKit(player)) {
            lang.sendMessage(player, "KIT.EDIT.saved", "kit", kitName);
        } else {
            lang.sendMessage(player, "KIT.EDIT.save-failed", "kit", kitName);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null; // No tab completion needed
    }
}
