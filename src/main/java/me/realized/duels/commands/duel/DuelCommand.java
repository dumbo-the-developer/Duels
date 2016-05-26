package me.realized.duels.commands.duel;

import me.realized.duels.arena.ArenaManager;
import me.realized.duels.commands.BaseCommand;
import me.realized.duels.commands.SubCommand;
import me.realized.duels.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.commands.duel.subcommands.DenyCommand;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.DataManager;
import me.realized.duels.data.UserData;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.dueling.Settings;
import me.realized.duels.kits.KitManager;
import me.realized.duels.utilities.PlayerUtil;
import me.realized.duels.utilities.inventory.Metadata;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelCommand extends BaseCommand {

    private final Config config;
    private final DataManager dataManager;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private final RequestManager requestManager;
    private final List<SubCommand> commands = new ArrayList<>();

    public DuelCommand() {
        super("duel", "duels.duel");
        commands.addAll(Arrays.asList(new AcceptCommand(), new DenyCommand()));
        this.config = getInstance().getConfiguration();
        this.dataManager = getInstance().getDataManager();
        this.kitManager = getInstance().getKitManager();
        this.arenaManager = getInstance().getArenaManager();
        this.requestManager = getInstance().getRequestManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage("Send, accept, or deny a duel request.\n/duel [player]\n/duel [accept | deny] [player]");
            return;
        }

        if (config.getBoolean("requires-cleared-inventory") && !PlayerUtil.hasEmptyInventory(player)) {
            pm(sender, "&cYour inventory must be empty to use duel commands.");
            return;
        }

        if (config.getBoolean("enabled") && !PlayerUtil.isInRegion(player, config.getString("region"))) {
           pm(sender, "&cYou must be in region '" + config.getString("region") + "' to use duel commands.");
           return;
        }

        else if (args.length == 1) {
            Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                pm(sender, "&cPlayer not found.");
                return;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                pm(sender, "&cYou may not duel yourself.");
                return;
            }

            UserData data = dataManager.getUser(target.getUniqueId(), false);

            if (data == null) {
                pm(sender, "&cPlayer not found.");
                return;
            }

            if (!data.canRequest()) {
                pm(sender, "&cThat player is currently not accepting requests.");
                return;
            }

            if (arenaManager.isInMatch(player) || arenaManager.isInMatch(target)) {
                pm(player, "&cEither you or that player is already in a match.");
                return;
            }

            if (requestManager.hasRequestTo(player, target) == RequestManager.Result.FOUND) {
                pm(sender, "&cYou already have a request sent to " + target.getName() + ".");
                return;
            }

            player.setMetadata("request", new Metadata(getInstance(), new Settings(target.getUniqueId())));
            player.openInventory(kitManager.getGUI().getFirst());
            return;
        }

        SubCommand subCommand = null;

        for (SubCommand command : commands) {
            if (args[0].equalsIgnoreCase(command.getName())) {
                subCommand = command;
                break;
            }
        }

        if (subCommand == null) {
            pm(sender, "&c'" + args[0] + "' is not a valid parent command.");
            return;
        }

        if (args.length < subCommand.length()) {
            pm(sender, "&7Usage: &f/" + getName() + " " + subCommand.getUsage() + " - " + subCommand.getDescription());
            return;
        }

        subCommand.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
