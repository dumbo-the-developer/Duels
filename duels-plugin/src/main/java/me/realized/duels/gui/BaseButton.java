package me.realized.duels.gui;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.kit.KitManager;
import me.realized.duels.request.RequestManager;
import me.realized.duels.setting.SettingManager;
import me.realized.duels.util.gui.Button;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class BaseButton extends Button<DuelsPlugin> {

    protected final Config config;
    protected final Lang lang;
    protected final ArenaManager arenaManager;
    protected final KitManager kitManager;
    protected final SettingManager settingManager;
    protected final RequestManager requestManager;

    protected BaseButton(final DuelsPlugin plugin, final ItemStack displayed) {
        super(plugin, displayed);

        if (displayed.hasItemMeta()) {
            final ItemMeta meta = displayed.getItemMeta();
            meta.addItemFlags(ItemFlag.values());
            displayed.setItemMeta(meta);
        }

        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.kitManager = plugin.getKitManager();
        this.settingManager = plugin.getSettingManager();
        this.requestManager = plugin.getRequestManager();
    }
}
