package me.realized.duels.hook;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.hook.hooks.BountyHuntersHook;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.EssentialsHook;
import me.realized.duels.hook.hooks.FactionsHook;
import me.realized.duels.hook.hooks.MVdWPlaceholderHook;
import me.realized.duels.hook.hooks.McMMOHook;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.hook.hooks.PlaceholderHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.SimpleClansHook;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.hook.hooks.WorldGuardHook;
import me.realized.duels.util.hook.AbstractHookManager;

public class HookManager extends AbstractHookManager<DuelsPlugin> {

    public HookManager(final DuelsPlugin plugin) {
        super(plugin);
        register("BountyHunters", BountyHuntersHook.class);
        register("CombatLogX", CombatLogXHook.class);
        register("CombatTagPlus", CombatTagPlusHook.class);
        register("Essentials", EssentialsHook.class);
        register("Factions", FactionsHook.class);
        register("mcMMO", McMMOHook.class);
        register("MVdWPlaceholderAPI", MVdWPlaceholderHook.class);
        register("MyPet", MyPetHook.class);
        register("PlaceholderAPI", PlaceholderHook.class);
        register("PvPManager", PvPManagerHook.class);
        register("SimpleClans", SimpleClansHook.class);
        register("Vault", VaultHook.class);
        register("WorldGuard", WorldGuardHook.class);
    }
}
