package me.realized.duels.gui;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.kit.KitManagerImpl;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.queue.sign.QueueSignManagerImpl;
import me.realized.duels.request.RequestManager;
import me.realized.duels.setting.SettingsManager;
import me.realized.duels.spectate.SpectateManagerImpl;
import me.realized.duels.util.gui.Button;
import org.bukkit.inventory.ItemStack;

public abstract class BaseButton extends Button<DuelsPlugin> {

    protected final Config config;
    protected final Lang lang;
    protected final KitManagerImpl kitManager;
    protected final ArenaManagerImpl arenaManager;
    protected final SettingsManager settingManager;
    protected final QueueManager queueManager;
    protected final QueueSignManagerImpl queueSignManager;
    protected final SpectateManagerImpl spectateManager;
    protected final RequestManager requestManager;

    protected BaseButton(final DuelsPlugin plugin, final ItemStack displayed) {
        super(plugin, displayed);
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.settingManager = plugin.getSettingManager();
        this.queueManager = plugin.getQueueManager();
        this.queueSignManager = plugin.getQueueSignManager();
        this.spectateManager = plugin.getSpectateManager();
        this.requestManager = plugin.getRequestManager();
    }
}
