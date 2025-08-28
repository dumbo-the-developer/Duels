package com.meteordevelopments.duels.startup;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.command.SubCommand;
import com.meteordevelopments.duels.command.commands.RankCommand;
import com.meteordevelopments.duels.command.commands.SpectateCommand;
import com.meteordevelopments.duels.command.commands.duel.DuelCommand;
import com.meteordevelopments.duels.command.commands.duels.DuelsCommand;
import com.meteordevelopments.duels.command.commands.party.PartyCommand;
import com.meteordevelopments.duels.command.commands.queue.QueueCommand;
import com.meteordevelopments.duels.util.CC;
import com.meteordevelopments.duels.util.command.AbstractCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles registration of all plugin commands
 */
public class CommandRegistrar {
    
    private final DuelsPlugin plugin;
    private final Map<String, AbstractCommand<DuelsPlugin>> commands = new HashMap<>();
    
    public CommandRegistrar(DuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers all plugin commands
     */
    public void registerCommands() {
        long start = System.currentTimeMillis();
        DuelsPlugin.sendMessage("&eRegistering commands...");
        
        registerCommand(new DuelCommand(plugin));
        registerCommand(new PartyCommand(plugin));
        registerCommand(new QueueCommand(plugin));
        registerCommand(new SpectateCommand(plugin));
        registerCommand(new DuelsCommand(plugin));
        registerCommand(new RankCommand(plugin));
        
        DuelsPlugin.sendMessage("&dSuccessfully registered commands [" + CC.getTimeDifferenceAndColor(start, System.currentTimeMillis()) + ChatColor.WHITE + "]");
    }
    
    /**
     * Registers a single command
     * @param command the command to register
     */
    private void registerCommand(AbstractCommand<DuelsPlugin> command) {
        commands.put(command.getName().toLowerCase(), command);
        command.register();
    }
    
    /**
     * Registers a subcommand to an existing command
     * @param commandName the parent command name
     * @param subCommand the subcommand to register
     * @return true if successful, false otherwise
     */
    public boolean registerSubCommand(String commandName, SubCommand subCommand) {
        Objects.requireNonNull(commandName, "command");
        Objects.requireNonNull(subCommand, "subCommand");

        final AbstractCommand<DuelsPlugin> result = commands.get(commandName.toLowerCase());

        if (result == null || result.isChild(subCommand.getName().toLowerCase())) {
            return false;
        }

        result.child(new AbstractCommand<DuelsPlugin>(plugin, subCommand) {
            @Override
            protected void execute(final CommandSender sender, final String label, final String[] args) {
                subCommand.execute(sender, label, args);
            }
        });
        return true;
    }
    
    /**
     * Clears all registered commands
     */
    public void clearCommands() {
        commands.clear();
    }
}