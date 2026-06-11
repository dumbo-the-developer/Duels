package com.meteordevelopments.duelsffa.command;

import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.arena.FfaArena;
import com.meteordevelopments.duelsffa.arena.FfaArenaManager;
import com.meteordevelopments.duelsffa.arena.FfaPlayerManager;
import com.meteordevelopments.duelsffa.arena.LeaveReason;
import com.meteordevelopments.duelsffa.config.Lang;
import com.meteordevelopments.duelsffa.selection.Selection;
import com.meteordevelopments.duelsffa.selection.SelectionManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class FfaCommand extends Command {

    private static final String PERM_USE = "duels.ffa";
    private static final String PERM_ADMIN = "duels.ffa.admin";

    private final FfaExtension extension;
    private final Lang lang;
    private final FfaArenaManager arenaManager;
    private final FfaPlayerManager playerManager;
    private final SelectionManager selectionManager;

    public FfaCommand(final FfaExtension extension) {
        super("ffa");
        this.extension = extension;
        this.lang = extension.getLang();
        this.arenaManager = extension.getArenaManager();
        this.playerManager = extension.getPlayerManager();
        this.selectionManager = extension.getSelectionManager();
        setDescription("FFA arenas");
        setUsage("/ffa");
        setAliases(Arrays.asList("duelsffa"));
    }

    @Override
    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.ffa.usage");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "join":
                return handleJoin(sender, args);
            case "leave":
                return handleLeave(sender);
            case "pos1":
                return handlePos(sender, true);
            case "pos2":
                return handlePos(sender, false);
            case "create":
                return handleCreate(sender, args);
            case "list":
                return handleList(sender);
            case "forceregenerate":
                return handleRegen(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args, true);
            case "addspawn":
                return handleSetSpawn(sender, args, false);
            case "enable":
                return handleEnable(sender, args, true);
            case "disable":
                return handleEnable(sender, args, false);
            default:
                lang.sendMessage(sender, "ERROR.invalid-sub-command", "argument", sub);
                return true;
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("join", "leave", "pos1", "pos2", "create", "list", "forceregenerate", "setspawn", "addspawn", "enable", "disable");
        }
        return super.tabComplete(sender, alias, args);
    }

    private boolean handleJoin(final CommandSender sender, final String[] args) {
        if (!requirePermission(sender, PERM_USE)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return true;
        }
        if (args.length < 2) {
            sendUsage(sender, "join <arena>");
            return true;
        }

        String name = args[1];
        FfaArena arena = arenaManager.getArena(name);
        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena-not-found", "name", name);
            return true;
        }

        Player player = (Player) sender;
        FfaPlayerManager.JoinResult result = playerManager.tryJoin(player, arena);
        if (!result.isSuccess()) {
            lang.sendMessage(sender, result.getMessageKey(), result.getPlaceholders());
            return true;
        }

        lang.sendMessage(sender, "COMMAND.ffa.join", "arena", arena.getName());
        return true;
    }

    private boolean handleLeave(final CommandSender sender) {
        if (!requirePermission(sender, PERM_USE)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return true;
        }

        Player player = (Player) sender;
        FfaArena arena = playerManager.leave(player, LeaveReason.COMMAND);
        if (arena == null) {
            lang.sendMessage(sender, "ERROR.not-in-ffa");
            return true;
        }

        lang.sendMessage(sender, "COMMAND.ffa.leave", "arena", arena.getName());
        return true;
    }

    private boolean handlePos(final CommandSender sender, final boolean first) {
        if (!requirePermission(sender, PERM_ADMIN)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return true;
        }

        Player player = (Player) sender;
        Block target = player.getTargetBlockExact(200);
        Location location = target != null ? target.getLocation() : player.getLocation();
        if (first) {
            selectionManager.setFirst(player, location);
        } else {
            selectionManager.setSecond(player, location);
        }
        return true;
    }

    private boolean handleCreate(final CommandSender sender, final String[] args) {
        if (!requirePermission(sender, PERM_ADMIN)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return true;
        }
        if (args.length < 3) {
            sendUsage(sender, "create <name> <kit|none>");
            return true;
        }

        Player player = (Player) sender;
        String name = args[1];
        String kit = args[2];

        Selection selection = selectionManager.get(player);
        if (selection == null || !selection.isSelected()) {
            lang.sendMessage(sender, "ERROR.no-selection");
            return true;
        }
        if (arenaManager.getArena(name) != null) {
            lang.sendMessage(sender, "ERROR.arena-already-exists", "name", name);
            return true;
        }

        if (!isNoKit(kit)) {
            com.meteordevelopments.duels.api.kit.Kit resolved = playerManager.resolveKit(kit);
            if (resolved == null) {
                lang.sendMessage(sender, "ERROR.kit-not-found", "name", kit);
                return true;
            }
            kit = resolved.getName();
        }

        boolean created = arenaManager.createArena(name, kit, selection, player.getLocation());
        if (!created) {
            lang.sendMessage(sender, "ERROR.create-failed", "name", name);
            return true;
        }

        lang.sendMessage(sender, "COMMAND.ffa.create", "arena", name, "kit", kit);
        return true;
    }

    private boolean handleList(final CommandSender sender) {
        if (!requirePermission(sender, PERM_USE)) {
            return true;
        }
        if (arenaManager.getArenas().isEmpty()) {
            lang.sendMessage(sender, "ERROR.no-arenas");
            return true;
        }
        lang.sendMessage(sender, "COMMAND.ffa.list.header");
        for (FfaArena arena : arenaManager.getArenas()) {
            lang.sendMessage(sender, "COMMAND.ffa.list.format",
                    "name", arena.getName(), "kit", arena.getKitName(), "enabled", arena.isEnabled());
        }
        lang.sendMessage(sender, "COMMAND.ffa.list.footer", "count", arenaManager.getArenas().size());
        return true;
    }

    private boolean handleRegen(final CommandSender sender, final String[] args) {
        if (!requirePermission(sender, PERM_ADMIN)) {
            return true;
        }
        if (args.length < 2) {
            sendUsage(sender, "forceregenerate <name>");
            return true;
        }
        String name = args[1];
        FfaArena arena = arenaManager.getArena(name);
        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena-not-found", "name", name);
            return true;
        }
        if (arena.getZone() == null) {
            lang.sendMessage(sender, "ERROR.no-zone", "name", name);
            return true;
        }
        lang.sendMessage(sender, "COMMAND.ffa.regen.start", "name", arena.getName());
        arenaManager.forceRegen(arena, () -> lang.sendMessage(sender, "COMMAND.ffa.regen.end", "name", arena.getName()));
        return true;
    }

    private boolean handleEnable(final CommandSender sender, final String[] args, final boolean enable) {
        if (!requirePermission(sender, PERM_ADMIN)) {
            return true;
        }
        if (args.length < 2) {
            sendUsage(sender, (enable ? "enable <name>" : "disable <name>"));
            return true;
        }
        String name = args[1];
        FfaArena arena = arenaManager.getArena(name);
        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena-not-found", "name", name);
            return true;
        }
        if (enable) {
            arenaManager.enableArena(arena);
            lang.sendMessage(sender, "COMMAND.ffa.enable", "name", arena.getName());
        } else {
            arenaManager.disableArena(arena);
            lang.sendMessage(sender, "COMMAND.ffa.disable", "name", arena.getName());
        }
        return true;
    }

    private boolean requirePermission(final CommandSender sender, final String permission) {
        if (sender.hasPermission(permission) || sender.hasPermission("duels.admin")) {
            return true;
        }
        lang.sendMessage(sender, "ERROR.no-permission");
        return false;
    }

    private void sendUsage(final CommandSender sender, final String usage) {
        lang.sendMessage(sender, "COMMAND.ffa.usage-single", "usage", usage);
    }

    private boolean isNoKit(final String kit) {
        if (kit == null) return true;
        String lower = kit.toLowerCase();
        return lower.equals("none") || lower.equals("no-kit") || lower.equals("nokit");
    }

    private boolean handleSetSpawn(final CommandSender sender, final String[] args, final boolean replace) {
        if (!requirePermission(sender, PERM_ADMIN)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player-only");
            return true;
        }
        if (args.length < 2) {
            sendUsage(sender, (replace ? "setspawn <name>" : "addspawn <name>"));
            return true;
        }
        String name = args[1];
        FfaArena arena = arenaManager.getArena(name);
        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena-not-found", "name", name);
            return true;
        }

        Player player = (Player) sender;
        if (replace) {
            arena.getSpawns().clear();
        }
        arena.addSpawn(player.getLocation());
        arenaManager.save();
        lang.sendMessage(sender, replace ? "COMMAND.ffa.setspawn" : "COMMAND.ffa.addspawn", "arena", arena.getName());
        return true;
    }
}
