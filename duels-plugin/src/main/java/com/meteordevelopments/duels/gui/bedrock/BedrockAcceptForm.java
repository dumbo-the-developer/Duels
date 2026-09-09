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
        final String customPrefix = plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-duel.custom-kit-prefix", "[Custom] "));
        final String kit;
        if (settings.getCustomKit() != null) {
            kit = customPrefix + settings.getCustomKit().getName();
        } else if (settings.getKit() != null) {
            kit = settings.getKit().getName();
        } else if (settings.isOwnInventory()) {
            kit = plugin.getLang().toLegacyString(plugin.getLang().getMessage("GENERAL.enabled"));
        } else {
            kit = plugin.getLang().toLegacyString(plugin.getLang().getMessage("GENERAL.not-selected"));
        }

        final String arena = settings.getArena() != null
                ? settings.getArena().getName()
                : plugin.getLang().toLegacyString(plugin.getLang().getMessage("GENERAL.random"));
        final int bet = settings.getBet();

        // Build the info content string
        final StringBuilder content = new StringBuilder();
        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.challenge", "&e%sender%&r has challenged you to a duel!\n\n")
                .replace("%sender%", sender.getName())));
        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.kit", "&7Kit: &f%kit%\n")
                .replace("%kit%", kit)));

        if (settings.isOwnInventory()) {
            content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.own-inventory", "&7Own Inventory: &aEnabled\n")));
        }

        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.arena", "&7Arena: &f%arena%\n")
                .replace("%arena%", arena)));

        if (bet > 0) {
            content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.bet", "&7Bet: &6$%bet%\n")
                    .replace("%bet%", String.valueOf(bet))));
        }

        if (settings.isItemBetting()) {
            content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.item-betting", "&7Item Betting: &aEnabled\n")));
        }

        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.question", "\n&fDo you accept?")));

        final com.meteordevelopments.duels.api.customkit.CustomKitSnapshot snapshot = settings.getCustomKitSnapshot() != null
                ? settings.getCustomKitSnapshot()
                : (settings.getCustomKit() != null ? settings.getCustomKit().toSnapshot() : null);

        final SimpleForm.Builder formBuilder = SimpleForm.builder()
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.title", "Duel Request")))
                .content(content.toString())
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.accept", "&a✔ Accept")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.deny", "&c✘ Deny")));

        if (snapshot != null) {
            formBuilder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-accept.preview-custom-kit", "&b👁 Preview Custom Kit")));
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
