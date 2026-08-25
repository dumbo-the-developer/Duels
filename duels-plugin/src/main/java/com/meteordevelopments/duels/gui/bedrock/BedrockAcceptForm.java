package com.meteordevelopments.duels.gui.bedrock;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Bedrock-safe SimpleForm for accepting or denying an incoming duel request.
 * Sent to Bedrock players when they receive a duel request, giving them
 * clear tap-friendly Accept / Deny buttons instead of relying on clickable
 * chat text (which is hard to use on mobile).
 */
public final class BedrockAcceptForm {

    private BedrockAcceptForm() {
    }

    /**
     * Sends a duel accept/deny form to a Bedrock target player.
     *
     * @param plugin   the plugin instance
     * @param sender   the player who sent the duel request
     * @param target   the Bedrock player who should accept or deny
     * @param settings the duel settings from the request (used for display only)
     */
    public static void send(final DuelsPlugin plugin, final Player sender, final Player target, final Settings settings) {
        final String kit;
        if (settings.getCustomKit() != null) {
            kit = "[Custom] " + settings.getCustomKit().getName();
        } else if (settings.getKit() != null) {
            kit = settings.getKit().getName();
        } else if (settings.isOwnInventory()) {
            kit = plugin.getLang().getMessage("GENERAL.enabled");
        } else {
            kit = plugin.getLang().getMessage("GENERAL.not-selected");
        }

        final String arena = settings.getArena() != null
                ? settings.getArena().getName()
                : plugin.getLang().getMessage("GENERAL.random");
        final int bet = settings.getBet();

        // Build the info content string
        final StringBuilder content = new StringBuilder();
        content.append("§e").append(sender.getName()).append("§r has challenged you to a duel!\n\n");
        content.append("§7Kit: §f").append(kit).append("\n");

        if (settings.isOwnInventory()) {
            content.append("§7Own Inventory: §aEnabled\n");
        }

        content.append("§7Arena: §f").append(arena).append("\n");

        if (bet > 0) {
            content.append("§7Bet: §6$").append(bet).append("\n");
        }

        if (settings.isItemBetting()) {
            content.append("§7Item Betting: §aEnabled\n");
        }

        content.append("\n§fDo you accept?");

        final com.meteordevelopments.duels.api.customkit.CustomKitSnapshot snapshot = settings.getCustomKitSnapshot() != null
                ? settings.getCustomKitSnapshot()
                : (settings.getCustomKit() != null ? settings.getCustomKit().toSnapshot() : null);

        final SimpleForm.Builder formBuilder = SimpleForm.builder()
                .title("Duel Request")
                .content(content.toString())
                .button("§a✔ Accept")
                .button("§c✘ Deny");

        if (snapshot != null) {
            formBuilder.button("§b👁 Preview Custom Kit");
        }

        formBuilder.validResultHandler(response -> {
            final int clicked = response.clickedButtonId();
            // Run the command as the player on the main thread
            DuelsPlugin.getFoliaLib().getScheduler().runNextTick(task -> {
                if (clicked == 0) {
                    // Accept
                    Bukkit.dispatchCommand(target, "duel accept " + sender.getName());
                } else if (clicked == 1) {
                    // Deny
                    Bukkit.dispatchCommand(target, "duel deny " + sender.getName());
                } else if (clicked == 2 && snapshot != null) {
                    // Preview Kit
                    BedrockCustomKitForm.openPreview(plugin, target, snapshot, () -> BedrockAcceptForm.send(plugin, sender, target, settings));
                }
            });
        });

        FloodgateApi.getInstance().sendForm(target.getUniqueId(), formBuilder.build());
    }
}
