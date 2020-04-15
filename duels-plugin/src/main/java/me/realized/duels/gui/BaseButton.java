package me.realized.duels.gui;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.kit.KitManager;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.queue.sign.QueueSignManager;
import me.realized.duels.request.RequestManager;
import me.realized.duels.setting.SettingsManager;
import me.realized.duels.spectate.SpectateManager;
import me.realized.duels.util.gui.Button;
import org.bukkit.inventory.ItemStack;

public abstract class BaseButton extends Button<DuelsPlugin> {

    protected final Config config;
    protected final Lang lang;
    protected final KitManager kitManager;
    protected final ArenaManager arenaManager;
    protected final SettingsManager settingManager;
    protected final QueueManager queueManager;
    protected final QueueSignManager queueSignManager;
    protected final SpectateManager spectateManager;
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
