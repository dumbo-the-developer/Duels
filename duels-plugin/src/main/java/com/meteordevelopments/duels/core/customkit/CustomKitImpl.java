package com.meteordevelopments.duels.core.customkit;

import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.api.customkit.CustomKitSnapshot;
import com.meteordevelopments.duels.api.event.customkit.CustomKitEquipEvent;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomKitImpl implements CustomKit {

    @Getter
    private final UUID uniqueId;
    @Getter
    private final UUID owner;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private List<String> description;
    @Getter
    @Setter
    private ItemStack icon;
    @Getter
    private final Map<Integer, ItemStack> items = new HashMap<>();
    @Getter
    private final Map<Integer, ItemStack> armor = new HashMap<>();
    @Getter
    @Setter
    private ItemStack offHand;
    @Getter
    private final long created;
    @Getter
    private long modified;

    public CustomKitImpl(@NotNull final UUID uniqueId,
                         @NotNull final UUID owner,
                         @NotNull final String name,
                         @Nullable final List<String> description,
                         @Nullable final ItemStack icon,
                         final long created,
                         final long modified) {
        this.uniqueId = Objects.requireNonNull(uniqueId, "uniqueId");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.name = Objects.requireNonNull(name, "name");
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
        this.icon = icon != null ? icon.clone() : ItemBuilder.of(Material.NETHERITE_SWORD).name("&b&l" + name).build();
        this.created = created > 0 ? created : System.currentTimeMillis();
        this.modified = modified > 0 ? modified : this.created;
    }

    public CustomKitImpl(@NotNull final UUID owner, @NotNull final String name) {
        this(UUID.randomUUID(), owner, name, Collections.emptyList(), null, System.currentTimeMillis(), System.currentTimeMillis());
    }

    @Override
    public void updateModified() {
        this.modified = System.currentTimeMillis();
    }

    @Override
    public boolean equip(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");

        final CustomKitEquipEvent event = new CustomKitEquipEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        final PlayerInventory inv = player.getInventory();
        inv.clear();

        // Equip 36 inventory slots (0-8 hotbar, 9-35 main inventory)
        for (int i = 0; i < 36; i++) {
            final ItemStack item = items.get(i);
            inv.setItem(i, item != null && item.getType() != Material.AIR ? item.clone() : null);
        }

        // Equip armor: Bukkit setArmorContents expects [Boots, Leggings, Chestplate, Helmet]
        // In our storage: 0 = Helmet, 1 = Chestplate, 2 = Leggings, 3 = Boots
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

    @NotNull
    @Override
    public CustomKitSnapshot toSnapshot() {
        return new CustomKitSnapshotImpl(this);
    }

    @NotNull
    @Override
    public CustomKit clone() {
        final CustomKitImpl copy = new CustomKitImpl(
                this.uniqueId,
                this.owner,
                this.name,
                new ArrayList<>(this.description),
                this.icon != null ? this.icon.clone() : null,
                this.created,
                this.modified
        );

        this.items.forEach((slot, item) -> {
            if (item != null && item.getType() != Material.AIR) {
                copy.items.put(slot, item.clone());
            }
        });

        this.armor.forEach((slot, item) -> {
            if (item != null && item.getType() != Material.AIR) {
                copy.armor.put(slot, item.clone());
            }
        });

        if (this.offHand != null && this.offHand.getType() != Material.AIR) {
            copy.offHand = this.offHand.clone();
        }

        return copy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomKitImpl that)) return false;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
