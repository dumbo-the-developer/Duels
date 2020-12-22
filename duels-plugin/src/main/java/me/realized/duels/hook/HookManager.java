package me.realized.duels.hook;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.hook.hooks.BountyHuntersHook;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.EssentialsHook;
import me.realized.duels.hook.hooks.FactionsHook;
import me.realized.duels.hook.hooks.LeaderHeadsHook;
import me.realized.duels.hook.hooks.MVdWPlaceholderHook;
import me.realized.duels.hook.hooks.McMMOHook;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.hook.hooks.PlaceholderHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.SimpleClansHook;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.util.hook.AbstractHookManager;

public class HookManager extends AbstractHookManager<DuelsPlugin> {

    public HookManager(final DuelsPlugin plugin) {
        super(plugin);
        register(BountyHuntersHook.NAME, BountyHuntersHook.class);
        register(CombatLogXHook.NAME, CombatLogXHook.class);
        register(CombatTagPlusHook.NAME, CombatTagPlusHook.class);
        register(EssentialsHook.NAME, EssentialsHook.class);
        register(FactionsHook.NAME, FactionsHook.class);
        register(LeaderHeadsHook.NAME, LeaderHeadsHook.class);
        register(McMMOHook.NAME, McMMOHook.class);
        register(MVdWPlaceholderHook.NAME, MVdWPlaceholderHook.class);
        register(MyPetHook.NAME, MyPetHook.class);
        register(PlaceholderHook.NAME, PlaceholderHook.class);
        register(PvPManagerHook.NAME, PvPManagerHook.class);
        register(SimpleClansHook.NAME, SimpleClansHook.class);
        register(VaultHook.NAME, VaultHook.class);
        register(WorldGuardHook.NAME, WorldGuardHook.class);
    }
}
