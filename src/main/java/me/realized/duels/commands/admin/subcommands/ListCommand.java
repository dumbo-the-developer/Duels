package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class ListCommand extends SubCommand {

    public ListCommand() {
        super("list", "list", "duels.admin", "Displays the lobby location and lists arenas and kits.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        String arenas = Helper.join(arenaManager.getArenaNames(), "&r, ");
        String kits = Helper.join(kitManager.getKitNames(), ", ");
        String lobby = (dataManager.getLobby() != null ? Helper.format(dataManager.getLobby()) : "Lobby not set, using world's spawn location. Set lobby using /duels setlobby");
        Helper.pm(sender, "Extra.list", true, "{ARENAS}", arenas, "{KITS}", kits, "{LOBBY}", lobby);
    }
}
