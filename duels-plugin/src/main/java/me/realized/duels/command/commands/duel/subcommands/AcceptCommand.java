package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.hooks.WorldGuardHook;
import me.realized.duels.request.Request;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BaseCommand {

    private final WorldGuardHook worldGuard;

    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a duel request.", 2);
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
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

        if (config.isDuelZoneEnabled() && worldGuard != null && !worldGuard.inDuelZone(player)) {
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

        final Settings settings = request.getSettings();
        final String kit = settings.getKit() != null ? settings.getKit().getName() : "Random";
        final String arena = settings.getArena() != null ? settings.getArena().getName() : "Random";
        final double betAmount = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(target, "COMMAND.duel.request.accepted.sender",
            "player", player.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(player, "COMMAND.duel.request.accepted.receiver",
            "player", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        if (settings.isItemBetting()) {
            settings.getLocations()[1] = player.getLocation().clone();
            bettingManager.open(settings, target, player);
        } else {
            duelManager.startMatch(player, target, settings, null, false);
        }
    }
}
