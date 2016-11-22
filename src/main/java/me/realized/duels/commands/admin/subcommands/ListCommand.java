package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ListCommand extends SubCommand {

    public ListCommand() {
        super("list", "list", "duels.admin", "Displays the lobby location and lists arenas and kits.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String arenas = Helper.join(arenaManager.getArenaNames(), "&r, ");
        String kits = Helper.join(kitManager.getKitNames(), ", ");
        String lobby = (dataManager.getLobby().equals(Bukkit.getWorlds().get(0).getSpawnLocation()) ? "lobby not set, using default world spawn location" : Helper.format(dataManager.getLobby()));
        Helper.pm(sender, "Extra.list", true, "{ARENAS}", arenas, "{KITS}", kits, "{LOBBY}", lobby);
    }
}
