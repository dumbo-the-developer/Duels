package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class CombatLogXHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "CombatLogX";

    public CombatLogXHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
    }

    public boolean isTagged(final Player player) {
        return false;
    }
}
