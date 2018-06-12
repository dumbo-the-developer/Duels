package me.realized._duels.commands;

import me.realized._duels.Core;
import me.realized._duels.arena.ArenaManager;
import me.realized._duels.configuration.ConfigType;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.configuration.MessagesConfig;
import me.realized._duels.data.DataManager;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.dueling.SpectatorManager;
import me.realized._duels.hooks.HookManager;
import me.realized._duels.kits.KitManager;
import me.realized._duels.utilities.Helper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand implements CommandExecutor {

    protected final MainConfig config = Core.getInstance().getConfiguration();
    protected final MessagesConfig messages = (MessagesConfig) Core.getInstance().getConfigManager().getConfigByType(ConfigType.MESSAGES);
    protected final RequestManager requestManager = Core.getInstance().getRequestManager();
    protected final DataManager dataManager = Core.getInstance().getDataManager();
    protected final ArenaManager arenaManager = Core.getInstance().getArenaManager();
    protected final SpectatorManager spectatorManager = Core.getInstance().getSpectatorManager();
    protected final KitManager kitManager = Core.getInstance().getKitManager();
    protected final HookManager hookManager = Core.getInstance().getHookManager();
    private final String command;
    private final String permission;

    protected BaseCommand(String command, String permission) {
        this.command = command;
        this.permission = permission;
    }

    public String getName() {
        return command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Helper.pm(sender, "&cThis command cannot be ran by " + sender.getName() + ".", false);
            return true;
        }

        if (!sender.hasPermission(permission)) {
            Helper.pm(sender, "Errors.no-permission", true);
            return true;
        }

        execute((Player) sender, args);
        return true;
    }

    public abstract void execute(Player sender, String[] args);
}
