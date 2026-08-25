package com.meteordevelopments.duels.core.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.api.customkit.CustomKitManager;
import com.meteordevelopments.duels.api.event.customkit.CustomKitCreateEvent;
import com.meteordevelopments.duels.api.event.customkit.CustomKitDeleteEvent;
import com.meteordevelopments.duels.api.event.customkit.CustomKitEditEvent;
import com.meteordevelopments.duels.core.customkit.config.CustomKitsConfig;
import com.meteordevelopments.duels.core.customkit.data.CustomKitStorage;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomKitManagerImpl implements Loadable, CustomKitManager {

    private final DuelsPlugin plugin;
    @Getter
    private final CustomKitsConfig customKitsConfig = new CustomKitsConfig();
    @Getter
    private final CustomKitStorage storage;
    @Getter
    private final CustomKitValidator validator;

    private final Map<UUID, List<CustomKitImpl>> kits = new ConcurrentHashMap<>();
    private final Map<UUID, CustomKitEditSession> activeSessions = new ConcurrentHashMap<>();

    public CustomKitManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.storage = new CustomKitStorage(plugin);
        this.validator = new CustomKitValidator(plugin);
    }

    @Override
    public void handleLoad() {
        customKitsConfig.load(plugin.getConfig());
        kits.clear();
        activeSessions.clear();
        kits.putAll(storage.loadAll());
        DuelsPlugin.sendMessage("&2Loaded custom kits for " + kits.size() + " player(s).");
    }

    @Override
    public void handleUnload() {
        activeSessions.clear();
        kits.clear();
    }

    @NotNull
    @Override
    public List<CustomKit> getKits(@NotNull final UUID owner) {
        final List<CustomKitImpl> list = kits.get(owner);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    @Nullable
    @Override
    public CustomKitImpl getKit(@NotNull final UUID owner, @NotNull final UUID id) {
        final List<CustomKitImpl> list = kits.get(owner);
        if (list == null) {
            return null;
        }
        for (final CustomKitImpl kit : list) {
            if (kit.getUniqueId().equals(id)) {
                return kit;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public CustomKitImpl getKit(@NotNull final UUID owner, @NotNull final String name) {
        final List<CustomKitImpl> list = kits.get(owner);
        if (list == null) {
            return null;
        }
        for (final CustomKitImpl kit : list) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public CustomKitImpl createKit(@NotNull final Player owner, @NotNull final String name) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");

        if (hasReachedLimit(owner)) {
            plugin.getLang().sendMessage(owner, "ERROR.customkits.limit-reached", "limit", getMaxKits(owner));
            return null;
        }

        final CustomKitValidator.ValidationResult val = validator.validateName(name, owner.getUniqueId(), null, customKitsConfig);
        if (!val.isValid()) {
            plugin.getLang().sendMessage(owner, val.getMessageKey(), val.getReplacers());
            return null;
        }

        if (getKit(owner.getUniqueId(), name) != null) {
            plugin.getLang().sendMessage(owner, "ERROR.customkits.name-duplicate", "name", name.trim());
            return null;
        }

        final CustomKitImpl kit = new CustomKitImpl(owner.getUniqueId(), name);
        kits.computeIfAbsent(owner.getUniqueId(), k -> new ArrayList<>()).add(kit);
        storage.savePlayer(owner.getUniqueId(), kits.get(owner.getUniqueId()));

        final CustomKitCreateEvent event = new CustomKitCreateEvent(owner, kit);
        Bukkit.getPluginManager().callEvent(event);

        return kit;
    }

    @Override
    public void saveKit(@NotNull final CustomKit kit) {
        Objects.requireNonNull(kit, "kit");
        if (!(kit instanceof CustomKitImpl impl)) {
            return;
        }

        impl.updateModified();
        final List<CustomKitImpl> list = kits.get(kit.getOwner());
        if (list != null) {
            storage.savePlayer(kit.getOwner(), list);
        }

        final Player player = Bukkit.getPlayer(kit.getOwner());
        if (player != null) {
            final CustomKitEditEvent event = new CustomKitEditEvent(player, kit);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Nullable
    @Override
    public CustomKitImpl deleteKit(@NotNull final UUID owner, @NotNull final UUID id) {
        final List<CustomKitImpl> list = kits.get(owner);
        if (list == null) {
            return null;
        }

        CustomKitImpl removed = null;
        final Iterator<CustomKitImpl> it = list.iterator();
        while (it.hasNext()) {
            final CustomKitImpl kit = it.next();
            if (kit.getUniqueId().equals(id)) {
                removed = kit;
                it.remove();
                break;
            }
        }

        if (removed != null) {
            storage.savePlayer(owner, list);
            final Player player = Bukkit.getPlayer(owner);
            if (player != null) {
                final CustomKitDeleteEvent event = new CustomKitDeleteEvent(player, removed);
                Bukkit.getPluginManager().callEvent(event);
            }
        }

        return removed;
    }

    @Nullable
    @Override
    public CustomKitImpl duplicateKit(@NotNull final Player owner, @NotNull final UUID kitId, @NotNull final String newName) {
        if (hasReachedLimit(owner)) {
            plugin.getLang().sendMessage(owner, "ERROR.customkits.limit-reached", "limit", getMaxKits(owner));
            return null;
        }

        final CustomKitImpl original = getKit(owner.getUniqueId(), kitId);
        if (original == null) {
            plugin.getLang().sendMessage(owner, "ERROR.customkits.not-found");
            return null;
        }

        final CustomKitValidator.ValidationResult val = validator.validateName(newName, owner.getUniqueId(), null, customKitsConfig);
        if (!val.isValid()) {
            plugin.getLang().sendMessage(owner, val.getMessageKey(), val.getReplacers());
            return null;
        }

        if (getKit(owner.getUniqueId(), newName) != null) {
            plugin.getLang().sendMessage(owner, "ERROR.customkits.name-duplicate", "name", newName.trim());
            return null;
        }

        final CustomKitImpl duplicated = (CustomKitImpl) original.clone();
        // Generate new unique ID for the duplicated kit
        final CustomKitImpl newKit = new CustomKitImpl(
                UUID.randomUUID(),
                owner.getUniqueId(),
                newName,
                new ArrayList<>(duplicated.getDescription()),
                duplicated.getIcon() != null ? duplicated.getIcon().clone() : null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        duplicated.getItems().forEach((slot, item) -> newKit.getItems().put(slot, item != null ? item.clone() : null));
        duplicated.getArmor().forEach((slot, item) -> newKit.getArmor().put(slot, item != null ? item.clone() : null));
        if (duplicated.getOffHand() != null) {
            newKit.setOffHand(duplicated.getOffHand().clone());
        }

        kits.computeIfAbsent(owner.getUniqueId(), k -> new ArrayList<>()).add(newKit);
        storage.savePlayer(owner.getUniqueId(), kits.get(owner.getUniqueId()));

        final CustomKitCreateEvent event = new CustomKitCreateEvent(owner, newKit);
        Bukkit.getPluginManager().callEvent(event);

        return newKit;
    }

    @Override
    public int getMaxKits(@NotNull final Player player) {
        if (player.hasPermission(Permissions.ADMIN) ||
            player.hasPermission(Permissions.CUSTOMKITS_ADMIN) ||
            player.hasPermission(customKitsConfig.getUnlimitedPermission())) {
            return Integer.MAX_VALUE;
        }

        int max = customKitsConfig.getDefaultLimit();

        for (final Map.Entry<String, Integer> entry : customKitsConfig.getPermissionLimits().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                max = Math.max(max, entry.getValue());
            }
        }

        // Also dynamically check duels.customkits.limit.<N>
        for (int i = 1; i <= 100; i++) {
            if (player.hasPermission(Permissions.CUSTOMKITS_LIMIT_PREFIX + i)) {
                max = Math.max(max, i);
            }
        }

        return max;
    }

    @Override
    public boolean hasReachedLimit(@NotNull final Player player) {
        final int max = getMaxKits(player);
        if (max == Integer.MAX_VALUE) {
            return false;
        }
        return getKits(player.getUniqueId()).size() >= max;
    }

    public CustomKitEditSession startSession(final Player player, final CustomKitImpl kit, final boolean isNew) {
        final CustomKitImpl originalCopy = isNew ? null : (CustomKitImpl) kit.clone();
        final CustomKitImpl draftCopy = (CustomKitImpl) kit.clone();
        final CustomKitEditSession session = new CustomKitEditSession(player.getUniqueId(), originalCopy, draftCopy, isNew);
        activeSessions.put(player.getUniqueId(), session);
        return session;
    }

    public CustomKitEditSession getSession(final Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public void endSession(final Player player) {
        activeSessions.remove(player.getUniqueId());
    }

    public void discardSession(final Player player) {
        final CustomKitEditSession session = getSession(player);
        if (session == null) {
            return;
        }

        if (session.isNew()) {
            final List<CustomKitImpl> list = kits.get(player.getUniqueId());
            if (list != null) {
                list.removeIf(k -> k.getUniqueId().equals(session.getKitId()));
                storage.savePlayer(player.getUniqueId(), list);
            }
        }
        endSession(player);
    }

    public boolean saveSession(final Player player) {
        final CustomKitEditSession session = getSession(player);
        if (session == null) {
            return false;
        }

        final CustomKitImpl draft = session.getDraftKit();
        final CustomKitValidator.ValidationResult val = validator.validateKit(draft, player, customKitsConfig);
        if (!val.isValid()) {
            plugin.getLang().sendMessage(player, val.getMessageKey(), val.getReplacers());
            return false;
        }

        final List<CustomKitImpl> list = kits.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUniqueId().equals(draft.getUniqueId())) {
                list.set(i, draft);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            list.add(draft);
        }

        saveKit(draft);
        endSession(player);
        return true;
    }
}
