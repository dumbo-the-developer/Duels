package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.spectate.SpectateManager.Result;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.spectate.SpectatorImpl;
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SpectateCommand extends BaseCommand {

    public SpectateCommand(final DuelsPlugin plugin) {
        super(plugin, "spectate", Permissions.SPECTATE, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final SpectatorImpl spectator = spectateManager.get(player);

        if (containsPlaceholder(args)) {
            lang.sendMessage(sender, "ERROR.command.invalid-argument", "arg", String.join(" ", args));
            return;
        }
        // If player is already spectating, using /spectate will put them out of spectator mode.
        if (spectator != null) {
            spectateManager.stopSpectating(player);
            lang.sendMessage(player, "COMMAND.spectate.stop-spectate", "name", spectator.getTargetName());
            return;
        }

        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.spectate.usage", "command", label);
            return;
        }

        if (config.isSpecRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return;
        }

        final Result result = spectateManager.startSpectating(player, target);

        switch (result) {
            case EVENT_CANCELLED:
                return;
            case IN_MATCH:
                lang.sendMessage(player, "ERROR.duel.already-spectating.sender");
                return;
            case IN_QUEUE:
                lang.sendMessage(player, "ERROR.duel.already-in-queue");
            case ALREADY_SPECTATING:
                lang.sendMessage(player, "ERROR.duel.already-in-match.sender");
                return;
            case TARGET_NOT_IN_MATCH:
                lang.sendMessage(player, "ERROR.duel.not-in-match", "name", target.getName());
                return;
            case SUCCESS:
                final ArenaImpl arena = arenaManager.get(target);

                // Meaningless checks to halt IDE warnings as target is guaranteed to be in a match if result is SUCCESS.
                if (arena == null || arena.getMatch() == null) {
                    return;
                }

                final DuelMatch match = arena.getMatch();
                final String kit = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
                lang.sendMessage(player, "COMMAND.spectate.start-spectate",
                        "name", target.getName(),
                        "opponent", Objects.requireNonNull(arena.getOpponent(target)).getName(),
                        "kit", kit,
                        "arena", arena.getName(),
                        "bet_amount", match.getBet()
                );
        }
    }

    private boolean containsPlaceholder(String[] args) {
        for (String arg : args) {
            if (arg.contains("%") || arg.contains("<") || arg.contains(">")) {
                return true;
            }
        }
        return false;
    }
}
