package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.config.Config.MessageSound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PlaysoundCommand extends BaseCommand {

    public PlaysoundCommand(final DuelsPlugin plugin) {
        super(plugin, "playsound", "playsound [name]", "Plays the selected sound if defined.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final MessageSound sound = config.getSound(args[1]);

        if (sound == null) {
            lang.sendMessage(sender, "ERROR.sound.not-found", "name", args[1]);
            return;
        }

        final Player player = (Player) sender;
        player.playSound(player.getLocation(), sound.getType(), sound.getVolume(), sound.getPitch());
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return config.getSounds().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
