package me.realized._duels.commands.admin.subcommands;

import me.realized._duels.commands.SubCommand;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.utilities.Helper;
import org.bukkit.entity.Player;

public class PlaysoundCommand extends SubCommand {

    public PlaysoundCommand() {
        super("playsound", "playsound [name]", "duels.admin", "Plays the selected sound if exists.", 2);
    }

    @Override
    public void execute(Player sender, String[] args) {
        MainConfig.CustomSound sound = config.getSound(args[1]);

        if (sound == null) {
            Helper.pm(sender, "Errors.sound-not-found", true);
            return;
        }

        sound.play(sender);
    }
}
