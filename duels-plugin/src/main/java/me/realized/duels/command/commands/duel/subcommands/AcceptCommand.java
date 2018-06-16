package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.request.Request;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BaseCommand {

    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a duel request.", 2);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.inventory-not-empty");
            return;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            // TODO: 16/06/2018 send msg
            return;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(sender, "ERROR.already-in-match.sender");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player-not-found", "name", args[1]);
            return;
        }

        final Request request = requestManager.remove(target, player);

        if (request == null) {
            lang.sendMessage(sender, "ERROR.no-request", "player", target.getName());
            return;
        }

        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(sender, "ERROR.already-in-match.target", "player", target.getName());
            return;
        }

        final Setting setting = request.getSetting();
        final String kit = setting.getKit() != null ? setting.getKit().getName() : "Random";
        final String arena = setting.getArena() != null ? setting.getArena().getName() : "Random";
        final double betAmount = setting.getBet();
        final String itemBetting = setting.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(target, "COMMAND.duel.request.accepted.sender",
            "player", player.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(player, "COMMAND.duel.request.accepted.receiver",
            "player", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        if (setting.isItemBetting()) {
            bettingManager.open(setting, target, player);
        } else {
            duelManager.startMatch(player, target, setting, null);
        }
    }
}
