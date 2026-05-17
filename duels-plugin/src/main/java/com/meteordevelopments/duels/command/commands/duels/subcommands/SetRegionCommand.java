package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.core.arena.ArenaImpl;
import com.meteordevelopments.duels.core.arena.RegionSelectionManager;
import com.meteordevelopments.duels.util.CC;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetRegionCommand extends BaseCommand {

    public SetRegionCommand(final DuelsPlugin plugin) {
        super(plugin, "setregion", "setregion [name]", "Setzt die Region einer Arena basierend auf der Wand-Auswahl.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtil.join(args, " ", 1, args.length);
        final ArenaImpl arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        Player player = (Player) sender;
        RegionSelectionManager.Selection selection = plugin.getRegionSelectionManager().getSelection(player);

        if (!selection.isComplete()) {
            player.sendMessage(CC.translate("&cDu musst erst beide Positionen mit der Wand markieren!"));
            return;
        }

        arena.setRegionPos1(selection.pos1);
        arena.setRegionPos2(selection.pos2);
        arenaManager.saveArenas();
        
        player.sendMessage(CC.translate("&aRegion für Arena &e" + name + " &aerfolgreich gesetzt!"));
    }
}
