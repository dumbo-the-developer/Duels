package com.meteordevelopments.duels.util.input;

import com.meteordevelopments.duels.DuelsPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {

    public static class ChatPrompt {
        private final UUID playerUuid;
        private final Consumer<String> onInput;
        private final Runnable onCancel;
        private final long expiresAt;

        public ChatPrompt(final UUID playerUuid, final Consumer<String> onInput, final Runnable onCancel, final long timeoutMs) {
            this.playerUuid = playerUuid;
            this.onInput = onInput;
            this.onCancel = onCancel;
            this.expiresAt = System.currentTimeMillis() + timeoutMs;
        }
    }

    private final DuelsPlugin plugin;
    private final Map<UUID, ChatPrompt> activePrompts = new ConcurrentHashMap<>();

    public ChatInputManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void prompt(final Player player, final String promptMessage, final Consumer<String> onInput, final Runnable onCancel) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(onInput, "onInput");

        activePrompts.put(player.getUniqueId(), new ChatPrompt(player.getUniqueId(), onInput, onCancel, 60000L));

        player.closeInventory();
        if (promptMessage != null && !promptMessage.isEmpty()) {
            player.sendMessage(plugin.getLang().toLegacyString(promptMessage));
        }
    }

    public void prompt(final Player player, final String promptMessage, final Consumer<String> onInput) {
        prompt(player, promptMessage, onInput, null);
    }

    public void cancel(final Player player) {
        final ChatPrompt prompt = activePrompts.remove(player.getUniqueId());
        if (prompt != null && prompt.onCancel != null) {
            DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onCancel.run());
        }
    }

    public boolean isPromptActive(final Player player) {
        return activePrompts.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final ChatPrompt prompt = activePrompts.remove(player.getUniqueId());

        if (prompt == null) {
            return;
        }

        event.setCancelled(true);

        if (System.currentTimeMillis() > prompt.expiresAt) {
            if (prompt.onCancel != null) {
                DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onCancel.run());
            }
            return;
        }

        final String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (text.equalsIgnoreCase("cancel")) {
            plugin.getLang().sendMessage(player, "GENERAL.cancelled");
            if (prompt.onCancel != null) {
                DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onCancel.run());
            }
            return;
        }

        DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onInput.accept(text));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final ChatPrompt prompt = activePrompts.remove(player.getUniqueId());

        if (prompt == null) {
            return;
        }

        event.setCancelled(true);

        if (System.currentTimeMillis() > prompt.expiresAt) {
            if (prompt.onCancel != null) {
                DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onCancel.run());
            }
            return;
        }

        final String text = event.getMessage().trim();

        if (text.equalsIgnoreCase("cancel")) {
            plugin.getLang().sendMessage(player, "GENERAL.cancelled");
            if (prompt.onCancel != null) {
                DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onCancel.run());
            }
            return;
        }

        DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> prompt.onInput.accept(text));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        activePrompts.remove(event.getPlayer().getUniqueId());
    }
}
