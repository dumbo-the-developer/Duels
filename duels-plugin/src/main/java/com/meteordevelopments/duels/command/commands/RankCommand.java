package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.rank.Rank;
import com.meteordevelopments.duels.util.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand extends BaseCommand {

    public RankCommand(final DuelsPlugin plugin) {
        super(plugin, "rank", "rank", "Shows your current rank information.", 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(CC.translate("&cThis command can only be used by players."));
            return;
        }

        if (!plugin.getRankManager().isEnabled()) {
            sender.sendMessage(CC.translate("&cRank system is currently disabled."));
            return;
        }

        Rank currentRank = plugin.getRankManager().getPlayerRank(player);
        if (currentRank == null) {
            sender.sendMessage(CC.translate(plugin.getLang().getMessage("RANK.not-found")));
            return;
        }

        // Send rank information
        sender.sendMessage(CC.translate("&9&l=== Rank Information ==="));
        sender.sendMessage(CC.translate("&7Rank: " + currentRank.getColoredName()));
        sender.sendMessage(CC.translate("&7Description: " + currentRank.getDescription()));
        
        // Show progress to next rank
        int currentElo = plugin.getUserManager().get(player).getTotalElo();
        double progress = currentRank.getProgress(currentElo);
        
        if (currentRank.getMaxElo() == Integer.MAX_VALUE) {
            sender.sendMessage(CC.translate(plugin.getLang().getMessage("RANK.max-rank")));
        } else {
            Rank nextRank = plugin.getRankManager().getNextRank(player);
            if (nextRank != null) {
                int eloNeeded = currentRank.getEloNeededForNextRank(currentElo);
                sender.sendMessage(CC.translate(String.format("&7Progress to &e%s&7: &e%.1f%%&7 (&e%d&7 ELO needed)", 
                    nextRank.getName(), progress, eloNeeded)));
            }
        }
        
        sender.sendMessage(CC.translate("&7Total ELO: &e" + currentElo));
    }
}
