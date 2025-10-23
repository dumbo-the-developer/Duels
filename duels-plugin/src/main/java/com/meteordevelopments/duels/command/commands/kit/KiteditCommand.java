package com.meteordevelopments.duels.command.commands.kit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.kit.edit.KitEditManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to start kit editing mode for a player.
 * Usage: /kitedit <kitname>
 */
public class KiteditCommand extends BaseCommand {
    
    public KiteditCommand(DuelsPlugin plugin) {
        super(plugin, "kitedit", "kitedit <kitname>", "Starts kit editing mode for the specified kit.", 2, true);
    }
    
    public void executeCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return;
        }
        
        if (args.length < 2) {
            lang.sendMessage(sender, "COMMAND.kit.edit.usage");
            return;
        }
        
        Player player = (Player) sender;
        String kitName = args[1];
        
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
        
        // Check if kit exists
        KitImpl kit = kitManager.get(kitName);
        if (kit == null) {
            lang.sendMessage(player, "ERROR.kit.not-found", "name", kitName);
            return;
        }
        
        // Start editing session
        if (KitEditManager.getInstance().startEditSession(player, kitName)) {
            lang.sendMessage(player, "KIT.EDIT.started", "kit", kitName);
        } else {
            lang.sendMessage(player, "KIT.EDIT.start-failed", "kit", kitName);
        }
    }
    
    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return;
        }
        
        Player player = (Player) sender;
        String kitName = args[1];
        
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
        
        // Check if kit exists
        KitImpl kit = kitManager.get(kitName);
        if (kit == null) {
            lang.sendMessage(player, "ERROR.kit.not-found", "name", kitName);
            return;
        }
        
        // Start editing session
        if (KitEditManager.getInstance().startEditSession(player, kitName)) {
            lang.sendMessage(player, "KIT.EDIT.started", "kit", kitName);
        } else {
            lang.sendMessage(player, "KIT.EDIT.start-failed", "kit", kitName);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return kitManager.getNames(true);
        }
        return null;
    }
}
