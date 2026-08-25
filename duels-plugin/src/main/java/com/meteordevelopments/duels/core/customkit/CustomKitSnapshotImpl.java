package com.meteordevelopments.duels.core.customkit;

import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.api.customkit.CustomKitSnapshot;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomKitSnapshotImpl implements CustomKitSnapshot {

    @Getter
    private final UUID kitId;
    @Getter
    private final UUID owner;
    @Getter
    private final String name;
    @Getter
    private final List<String> description;
    @Getter
    private final ItemStack icon;
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, ItemStack> armor = new HashMap<>();
    private final ItemStack offHand;

    public CustomKitSnapshotImpl(@NotNull final CustomKit kit) {
        this.kitId = kit.getUniqueId();
        this.owner = kit.getOwner();
        this.name = kit.getName();
        this.description = Collections.unmodifiableList(new ArrayList<>(kit.getDescription()));
        this.icon = kit.getIcon().clone();

        kit.getItems().forEach((slot, item) -> {
            if (item != null && item.getType() != Material.AIR) {
                this.items.put(slot, item.clone());
            }
        });

        kit.getArmor().forEach((slot, item) -> {
            if (item != null && item.getType() != Material.AIR) {
                this.armor.put(slot, item.clone());
            }
        });

        this.offHand = kit.getOffHand() != null && kit.getOffHand().getType() != Material.AIR
                ? kit.getOffHand().clone() : null;
    }

    @NotNull
    @Override
    public Map<Integer, ItemStack> getItems() {
        final Map<Integer, ItemStack> copy = new HashMap<>();
        items.forEach((slot, item) -> copy.put(slot, item != null ? item.clone() : null));
        return Collections.unmodifiableMap(copy);
    }

    @NotNull
    @Override
    public Map<Integer, ItemStack> getArmor() {
        final Map<Integer, ItemStack> copy = new HashMap<>();
        armor.forEach((slot, item) -> copy.put(slot, item != null ? item.clone() : null));
        return Collections.unmodifiableMap(copy);
    }

    @Nullable
    @Override
    public ItemStack getOffHand() {
        return offHand != null ? offHand.clone() : null;
    }

    @Override
    public boolean equip(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");

        final PlayerInventory inv = player.getInventory();
        inv.clear();

        for (int i = 0; i < 36; i++) {
            final ItemStack item = items.get(i);
            inv.setItem(i, item != null && item.getType() != Material.AIR ? item.clone() : null);
        }

        final ItemStack helmet = armor.get(0);
        final ItemStack chestplate = armor.get(1);
        final ItemStack leggings = armor.get(2);
        final ItemStack boots = armor.get(3);

        inv.setHelmet(helmet != null && helmet.getType() != Material.AIR ? helmet.clone() : null);
        inv.setChestplate(chestplate != null && chestplate.getType() != Material.AIR ? chestplate.clone() : null);
        inv.setLeggings(leggings != null && leggings.getType() != Material.AIR ? leggings.clone() : null);
        inv.setBoots(boots != null && boots.getType() != Material.AIR ? boots.clone() : null);

        inv.setItemInOffHand(offHand != null && offHand.getType() != Material.AIR ? offHand.clone() : null);

        player.updateInventory();
        return true;
    }
}
