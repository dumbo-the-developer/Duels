package me.realized.duels.commands.admin.subcommands;

import me.realized.duels.commands.SubCommand;
import me.realized.duels.utilities.Helper;
import org.bukkit.entity.Player;

public class SetlobbyCommand extends SubCommand {

    public SetlobbyCommand() {
        super("setlobby", "setlobby", "duels.admin", "Set duel lobby on your current location.", 1);
    }

    @Override
    public void execute(Player sender, String[] args) {
        dataManager.setLobby(sender);
        Helper.pm(sender, "Extra.set-lobby", true);
    }
}
