package me.realized.duels.hooks;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class PvPManagerHook extends PluginHook<DuelsPlugin> {

    private final Config config;

    public PvPManagerHook(final DuelsPlugin plugin) {
        super(plugin, "PvPManager");
        this.config = plugin.getConfiguration();
    }

    public boolean isTagged(final Player player) {
        if (config.isPmPreventDuel()) {
            return false;
        }

        final PlayerHandler playerHandler = ((PvPManager) getPlugin()).getPlayerHandler();
        final PvPlayer pvPlayer = playerHandler.get(player);
        return pvPlayer != null && pvPlayer.isInCombat();
    }

    public void removeTag(final Player player) {
        if (!config.isPmUntag()) {
            return;
        }

        final PlayerHandler playerHandler = ((PvPManager) getPlugin()).getPlayerHandler();
        final PvPlayer pvPlayer = playerHandler.get(player);

        if (pvPlayer == null) {
            return;
        }

        playerHandler.untag(pvPlayer);
    }
}
