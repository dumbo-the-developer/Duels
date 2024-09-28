package com.meteordevelopments.duels.betting;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.betting.BettingGui;
import com.meteordevelopments.duels.hook.hooks.VaultHook;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.gui.GuiListener;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BettingManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final GuiListener<DuelsPlugin> guiListener;

    public BettingManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.guiListener = plugin.getGuiListener();
    }

    @Override
    public void handleLoad() {
        final VaultHook vaultHook = plugin.getHookManager().getHook(VaultHook.class);

        if (vaultHook == null) {
            DuelsPlugin.sendMessage("&bVault was not found! Money betting feature will be automatically disabled.");
        } else if (vaultHook.getEconomy() == null) {
            DuelsPlugin.sendMessage("&bEconomy plugin supporting Vault was not found! Money betting feature will be automatically disabled.");
        }
    }

    @Override
    public void handleUnload() {
    }

    public void open(final Settings settings, final Player first, final Player second) {
        final BettingGui gui = new BettingGui(plugin, settings, first, second);
        guiListener.addGui(first, gui).open(first);
        guiListener.addGui(second, gui).open(second);
    }
}
