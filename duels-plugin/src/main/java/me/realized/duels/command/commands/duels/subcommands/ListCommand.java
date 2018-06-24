package me.realized.duels.command.commands.duels.subcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.Kit;
import me.realized.duels.queue.QueueSign;
import me.realized.duels.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class ListCommand extends BaseCommand {

    public ListCommand(final DuelsPlugin plugin) {
        super(plugin, "list", null, null, 1, false, "ls");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final List<String> arenas = new ArrayList<>();
        arenaManager.getArenas().forEach(arena -> arenas.add("&" + getColor(arena) + arena.getName()));
        final String kits = StringUtils.join(kitManager.getKits().stream().map(Kit::getName).collect(Collectors.toList()), ", ");
        final String signs = StringUtils.join(queueManager.getSigns().stream().map(QueueSign::toString).collect(Collectors.toList()), ", ");
        lang.sendMessage(sender, "COMMAND.duels.list",
            "arenas", !arenas.isEmpty() ? StringUtils.join(arenas, "&r, &r") : "none",
            "kits", !kits.isEmpty() ? kits : "none",
            "signs", !signs.isEmpty() ? signs : "none",
            "lobby", StringUtil.parse(playerManager.getLobby()));
    }

    private String getColor(final Arena arena) {
        return arena.isDisabled() ? "4" : (arena.getPositions().size() < 2 ? "9" : arena.isUsed() ? "c" : "a");
    }
}