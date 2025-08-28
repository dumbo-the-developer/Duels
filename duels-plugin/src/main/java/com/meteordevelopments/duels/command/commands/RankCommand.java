package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand extends BaseCommand {

    public RankCommand(final DuelsPlugin plugin) {
        super(plugin, "rank", "rank", "Shows your current rank information.", "duels.command.rank", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        Player player = (Player) sender;

        if (!plugin.getRankManager().isEnabled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cRank system is currently disabled."));
            return;
        }

        Rank currentRank = plugin.getRankManager().getPlayerRank(player);
        if (currentRank == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLang().getMessage("RANK.not-found")));
            return;
        }

        // Send rank information
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9&l=== Rank Information ==="));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Rank: " + currentRank.getColoredName()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Description: " + currentRank.getDescription()));
        
        // Show progress to next rank
        var userData = plugin.getUserManager().get(player);
        if (userData == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnable to load your user data."));
            return;
        }
        
        int currentElo = userData.getTotalElo();
        double progress = currentRank.getProgress(currentElo);
        
        Rank nextRank = plugin.getRankManager().getNextRank(player);
        if (nextRank == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLang().getMessage("RANK.max-rank")));
        } else {
            int eloNeeded = Math.max(0, nextRank.getMinElo() - currentElo);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Progress to &e%s&7: &e%.1f%%&7 (&e%d&7 ELO needed)", 
                nextRank.getName(), progress, eloNeeded)));
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Total ELO: &e" + currentElo));
    }
}
