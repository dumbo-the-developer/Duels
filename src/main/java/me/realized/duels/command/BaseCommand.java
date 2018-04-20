package me.realized.duels.command;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.data.UserDataManager;
import me.realized.duels.kit.KitManager;
import me.realized.duels.util.command.AbstractCommand;
import me.realized.duels.util.gui.GuiListener;

public abstract class BaseCommand extends AbstractCommand<DuelsPlugin> {

    protected final DuelsPlugin plugin;
    protected final UserDataManager userManager;
    protected final GuiListener guiListener;
    protected final ArenaManager arenaManager;
    protected final KitManager kitManager;
    protected final SettingCache settingCache;

    public BaseCommand(final DuelsPlugin plugin, final String name, final String usage, final String permission, final int length, final boolean playerOnly,
        final String... aliases) {
        super(plugin, name, usage, permission, length, playerOnly, aliases);
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.guiListener = plugin.getGuiListener();
        this.arenaManager = plugin.getArenaManager();
        this.kitManager = plugin.getKitManager();
        this.settingCache = plugin.getSettingCache();
    }

    public BaseCommand(final DuelsPlugin plugin, final String name, final String permission, final boolean playerOnly) {
        this(plugin, name, null, permission, -1, playerOnly);
    }
}
