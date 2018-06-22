package me.realized.duels.hooks;

import java.util.Arrays;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Log;
import me.realized.duels.util.hook.PluginHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends PluginHook<DuelsPlugin> {

    @Getter
    private Economy economy;

    public VaultHook(final DuelsPlugin plugin) {
        super(plugin, "Vault");

        final RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (provider == null) {
            Log.warn("Found no available economy plugin that supports Vault. Money betting will not be available.");
            return;
        }

        economy = provider.getProvider();
    }

    public boolean has(final int amount, final Player... players) {
        if (economy == null) {
            return false;
        }

        for (final Player player : players) {
            if (!economy.has(player, amount)) {
                return false;
            }
        }

        return true;
    }

    public void add(final int amount, final Player... players) {
        if (amount > 0 && economy != null) {
            Arrays.stream(players).forEach(player -> economy.depositPlayer(player, amount));
        }
    }

    public void remove(final int amount, final Player... players) {
        if (amount > 0 && economy != null) {
            Arrays.stream(players).forEach(player -> economy.withdrawPlayer(player, amount));
        }
    }
}
