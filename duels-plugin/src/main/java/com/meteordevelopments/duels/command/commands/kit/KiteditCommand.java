package com.meteordevelopments.duels.command.commands.kit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.gui.kitedit.KitEditGui;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.kit.edit.KitEditManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to start kit editing mode for a player.
 * Usage: /kitedit [kitname] | Opens GUI if no kit specified
 */
public class KiteditCommand extends BaseCommand {
    
    public KiteditCommand(DuelsPlugin plugin) {
        super(plugin, "kitedit", "kitedit [kitname]", "Starts kit editing mode for the specified kit.", 1, true);
    }
    
    public void executeCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        // If no kit argument provided or only "cancel" without being in edit mode, open GUI
        if (args.length < 2) {
            openKitEditGui(player);
            return;
        }

        String kitName = args[1];

        // Handle cancel subcommand: /kit edit cancel
        if (kitName.equalsIgnoreCase("cancel")) {
            if (!KitEditManager.getInstance().isEditing(player)) {
                // Not editing, open GUI instead
                openKitEditGui(player);
                return;
            }

            KitEditManager.getInstance().abortEditSession(player);
            lang.sendMessage(player, "KIT.EDIT.cancelled");
            return;
        }
        
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
    
    private void openKitEditGui(Player player) {
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
        
        // Get all kits and convert to KitImpl list
        List<Kit> kits = kitManager.getKits();
        List<KitImpl> kitImpls = new ArrayList<>();
        for (Kit kit : kits) {
            if (kit instanceof KitImpl) {
                kitImpls.add((KitImpl) kit);
            }
        }
        
        // Create and open GUI
        KitEditGui gui = new KitEditGui(plugin, kitImpls);
        plugin.getGuiListener().addGui(player, gui);
        gui.open(player);
    }
    
    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        // If no kit argument provided or only "cancel" without being in edit mode, open GUI
        if (args.length < 2) {
            openKitEditGui(player);
            return;
        }

        String kitName = args[1];

        // Handle cancel subcommand: /kit edit cancel
        if (kitName.equalsIgnoreCase("cancel")) {
            if (!KitEditManager.getInstance().isEditing(player)) {
                // Not editing, open GUI instead
                openKitEditGui(player);
                return;
            }

            KitEditManager.getInstance().abortEditSession(player);
            lang.sendMessage(player, "KIT.EDIT.cancelled");
            return;
        }
        
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
            List<String> completions = new ArrayList<>(kitManager.getNames(false));
            completions.add("cancel");
            return completions;
        }
        return null;
    }
}
