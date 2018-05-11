package me.realized.duels.gui;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.kit.KitManager;
import me.realized.duels.request.RequestManager;
import me.realized.duels.util.gui.Button;
import org.bukkit.inventory.ItemStack;

public abstract class BaseButton extends Button {

    protected final Config config;
    protected final Lang lang;
    protected final ArenaManager arenaManager;
    protected final KitManager kitManager;
    protected final SettingCache settingCache;
    protected final RequestManager requestManager;

    public BaseButton(final DuelsPlugin plugin, final ItemStack displayed) {
        super(displayed);
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.kitManager = plugin.getKitManager();
        this.settingCache = plugin.getSettingCache();
        this.requestManager = plugin.getRequestManager();
    }
}
