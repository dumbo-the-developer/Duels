package me.realized.duels.command;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.config.Lang;
import me.realized.duels.data.UserDataManager;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.kit.KitManager;
import me.realized.duels.request.RequestManager;
import me.realized.duels.util.command.AbstractCommand;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.command.CommandSender;

public abstract class BaseCommand extends AbstractCommand<DuelsPlugin> {

    protected final DuelsPlugin plugin;
    protected final Lang lang;
    protected final UserDataManager userManager;
    protected final ArenaManager arenaManager;
    protected final KitManager kitManager;
    protected final SettingCache settingCache;
    protected final DuelManager duelManager;
    protected final RequestManager requestManager;

    public BaseCommand(final DuelsPlugin plugin, final String name, final String usage, final String description, final String permission, final int length, final boolean playerOnly,
        final String... aliases) {
        super(plugin, name, usage, description, permission, length, playerOnly, aliases);
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.kitManager = plugin.getKitManager();
        this.settingCache = plugin.getSettingCache();
        this.duelManager = plugin.getDuelManager();
        this.requestManager = plugin.getRequestManager();
    }

    public BaseCommand(final DuelsPlugin plugin, final String name, final String permission, final boolean playerOnly) {
        this(plugin, name, null, null, permission, -1, playerOnly);
    }

    @Override
    protected void handleMessage(final CommandSender sender, final MessageType type, final String... args) {
        switch (type) {
            case PLAYER_ONLY:
                super.handleMessage(sender, type, args);
                break;
            case NO_PERMISSION:
                lang.sendMessage(sender, "ERROR.no-permission", "permission", args[0]);
                break;
            case SUB_COMMAND_INVALID:
                lang.sendMessage(sender, "ERROR.invalid-sub-command", "command", args[0], "argument", args[1]);
                break;
            case SUB_COMMAND_USAGE:
                lang.sendMessage(sender, "COMMAND.sub-command-usage", "command", args[0], "usage", args[1], "description", args[2]);
                break;
        }
    }
}
