package me.realized.duels.hooks;

import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Log;
import me.realized.duels.util.hook.PluginHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends PluginHook<DuelsPlugin> {

    @Getter
    private Economy economy;

    public VaultHook(final DuelsPlugin plugin) {
        super(plugin, "Vault");

        final RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (provider == null) {
            Log.error("Found no available economy plugin that implements Vault Economy, functions such as betting will be disabled.");
            return;
        }

        economy = provider.getProvider();
    }

    public boolean hasEconomy() {
        return getEconomy() != null;
    }
}
