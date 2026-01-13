package com.meteordevelopments.duels.hook.hooks;

import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.hook.PluginHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.Collection;

@Getter
public class VaultHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "Vault";

    private Economy economy;

    public VaultHook(final DuelsPlugin plugin) {
        super(plugin, NAME);

        final RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (provider == null) {
            Log.warn("Found no available economy plugin that supports Vault. Money betting will not be available.");
            return;
        }

        economy = provider.getProvider();
        Log.info("Using Economy Provider: " + economy.getClass().getName());
    }

    public boolean has(final int amount, final Collection<Player> players) {
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

    public boolean has(final int amount, final Player... players) {
        return has(amount, Arrays.asList(players));
    }

    public void add(final int amount, final Collection<Player> players) {
        if (economy != null) {
            players.forEach(player -> economy.depositPlayer(player, amount));
        }
    }

    public void add(final int amount, final Player... players) {
        add(amount, Arrays.asList(players));
    }


    public void remove(final int amount, final Collection<Player> players) {
        if (economy != null) {
            players.forEach(player -> economy.withdrawPlayer(player, amount));
        }
    }

    public void remove(final int amount, final Player... players) {
        remove(amount, Arrays.asList(players));
    }

}