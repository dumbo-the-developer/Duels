package com.meteordevelopments.duels.gui.bedrock;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.core.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.core.kit.KitImpl;
import com.meteordevelopments.duels.core.kit.KitManagerImpl;
import com.meteordevelopments.duels.hook.hooks.VaultHook;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.NumberUtil;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Bedrock-safe CustomForm for configuring and sending a duel request.
 * Replaces the Java inventory GUI for Bedrock players.
 * <p>
 * Form layout (component indices):
 * <ul>
 *   <li>0 — Kit dropdown</li>
 *   <li>1 — Arena dropdown</li>
 *   <li>2 — Bet toggle  (only if money betting enabled)</li>
 *   <li>3 — Bet amount input (only if money betting enabled)</li>
 * </ul>
 */
public final class BedrockDuelForm {

    private BedrockDuelForm() {
    }

    /**
     * Opens the duel configuration form for a Bedrock player.
     *
     * @param plugin   the plugin instance
     * @param player   the Bedrock sender
     * @param target   the duel target
     * @param settings the player's current duel settings (already has target set)
     */
    public static void open(final DuelsPlugin plugin, final Player player, final Player target, final Settings settings) {
        final Config config = plugin.getConfiguration();
        final KitManagerImpl kitManager = plugin.getKitManager();
        final ArenaManagerImpl arenaManager = plugin.getArenaManager();

        // --- Build kit options ---
        final List<String> kitOptions = new ArrayList<>();
        if (config.isOwnInventoryEnabled()) {
            kitOptions.add("Own Inventory");
        }

        final List<com.meteordevelopments.duels.api.customkit.CustomKit> customKits = plugin.getCustomKitManager().getKits(player.getUniqueId());
        for (final com.meteordevelopments.duels.api.customkit.CustomKit ck : customKits) {
            kitOptions.add("[Custom] " + ck.getName());
        }

        final List<String> kitNames = kitManager.getNames(false);
        for (final String sk : kitNames) {
            kitOptions.add("[Server] " + sk);
        }

        // --- Build arena options ---
        final List<String> arenaOptions = new ArrayList<>();
        arenaOptions.add("Random");
        arenaOptions.addAll(arenaManager.getNames());

        // --- Build the form ---
        final boolean moneyBettingAvailable = config.isMoneyBettingEnabled();

        final CustomForm.Builder builder = CustomForm.builder()
                .title("Duel " + target.getName());

        // Component 0: Kit dropdown
        if (config.isKitSelectingEnabled() || config.isOwnInventoryEnabled() || !customKits.isEmpty()) {
            builder.dropdown("Select Kit", kitOptions);
        }

        // Component 1: Arena dropdown
        if (config.isArenaSelectingEnabled()) {
            builder.dropdown("Select Arena", arenaOptions);
        }

        // Components 2 & 3: Betting (only if money betting is enabled in config)
        if (moneyBettingAvailable) {
            builder.toggle("Enable Bet", false);
            builder.input("Bet Amount", "0");
        }

        // Track component indices dynamically
        final boolean hasKitDropdown = config.isKitSelectingEnabled() || config.isOwnInventoryEnabled() || !customKits.isEmpty();
        final boolean hasArenaDropdown = config.isArenaSelectingEnabled();
        final int kitIndex = 0;
        final int arenaIndex = hasKitDropdown ? 1 : 0;
        final int betToggleIndex = (hasKitDropdown ? 1 : 0) + (hasArenaDropdown ? 1 : 0);
        final int betAmountIndex = betToggleIndex + 1;

        builder.validResultHandler(response -> {
            // --- Kit ---
            if (hasKitDropdown) {
                final int kitSelection = response.asDropdown(kitIndex);
                int offset = 0;

                if (config.isOwnInventoryEnabled()) {
                    if (kitSelection == 0) {
                        if (config.isOwnInventoryUsePermission()
                                && !player.hasPermission(Permissions.OWN_INVENTORY)
                                && !player.hasPermission(Permissions.SETTING_ALL)) {
                            plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.OWN_INVENTORY);
                            return;
                        }
                        settings.setOwnInventory(true);
                        offset = -1; // handled
                    } else {
                        offset = 1;
                    }
                }

                if (offset != -1) {
                    final int adjustedIndex = kitSelection - offset;
                    if (adjustedIndex >= 0 && adjustedIndex < customKits.size()) {
                        // Custom Kit selected
                        final com.meteordevelopments.duels.api.customkit.CustomKit chosen = customKits.get(adjustedIndex);
                        settings.setCustomKit(chosen);
                    } else {
                        // Server Kit selected
                        final int serverKitIndex = adjustedIndex - customKits.size();
                        if (serverKitIndex >= 0 && serverKitIndex < kitNames.size()) {
                            final String kitName = kitNames.get(serverKitIndex);
                            final KitImpl kit = kitManager.get(kitName);

                            if (kit != null) {
                                final String permission = String.format(Permissions.KIT, kitName.replace(" ", "-").toLowerCase());
                                if (kit.isUsePermission()
                                        && !player.hasPermission(Permissions.KIT_ALL)
                                        && !player.hasPermission(permission)) {
                                    plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", permission);
                                    return;
                                }
                                settings.setKit(kit);
                            }
                        }
                    }
                }
            }

            // --- Arena ---
            if (hasArenaDropdown) {
                final int arenaSelection = response.asDropdown(arenaIndex);
                if (arenaSelection > 0) {
                    // A specific arena was selected (index 0 = "Random")
                    final List<String> arenaNames = arenaManager.getNames();
                    final int arenaNameIndex = arenaSelection - 1;
                    if (arenaNameIndex >= 0 && arenaNameIndex < arenaNames.size()) {
                        final var arena = arenaManager.get(arenaNames.get(arenaNameIndex));
                        if (arena != null) {
                            settings.setArena(arena);
                        }
                    }
                }
                // arenaSelection == 0 means "Random", arena stays null (random selection)
            }

            // --- Betting ---
            if (moneyBettingAvailable) {
                final boolean betEnabled = response.asToggle(betToggleIndex);
                if (betEnabled) {
                    if (config.isMoneyBettingUsePermission()
                            && !player.hasPermission(Permissions.MONEY_BETTING)
                            && !player.hasPermission(Permissions.SETTING_ALL)) {
                        plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.MONEY_BETTING);
                        return;
                    }

                    final String betText = response.asInput(betAmountIndex);
                    final int amount = NumberUtil.parseInt(betText).orElse(0);

                    if (amount > 0) {
                        final VaultHook vault = plugin.getHookManager().getHook(VaultHook.class);
                        if (vault == null || vault.getEconomy() == null) {
                            plugin.getLang().sendMessage(player, "ERROR.setting.disabled-option",
                                    "option", plugin.getLang().getMessage("GENERAL.betting"));
                            return;
                        }

                        if (!vault.getEconomy().has(player, amount)) {
                            plugin.getLang().sendMessage(player, "ERROR.command.not-enough-money");
                            return;
                        }

                        settings.setBet(amount);
                    }
                }
            }

            // --- Send the duel request using the existing pipeline ---
            plugin.getRequestManager().send(player, target, settings);
        });

        builder.closedOrInvalidResultHandler(() -> {
            // Player closed the form — do nothing (duel cancelled silently)
        });

        // Send the form to the Bedrock player
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
