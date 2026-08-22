package com.meteordevelopments.duels.core.customkit.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.data.ItemData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomKitData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private List<String> description = new ArrayList<>();

    @JsonProperty("icon")
    private ItemData icon;

    @JsonProperty("items")
    private Map<Integer, ItemData> items = new HashMap<>();

    @JsonProperty("armor")
    private Map<Integer, ItemData> armor = new HashMap<>();

    @JsonProperty("offHand")
    private ItemData offHand;

    @JsonProperty("created")
    private long created;

    @JsonProperty("modified")
    private long modified;

    public CustomKitData() {
    }

    public static CustomKitData fromCustomKit(final CustomKitImpl kit) {
        final CustomKitData data = new CustomKitData();
        data.setId(kit.getUniqueId().toString());
        data.setOwner(kit.getOwner().toString());
        data.setName(kit.getName());
        data.setDescription(new ArrayList<>(kit.getDescription()));
        data.setIcon(kit.getIcon() != null ? ItemData.fromItemStack(kit.getIcon()) : null);

        kit.getItems().forEach((slot, item) -> {
            if (item != null && item.getType() != Material.AIR) {
                data.getItems().put(slot, ItemData.fromItemStack(item));
            }
        });

        kit.getArmor().forEach((slot, item) -> {
            if (item != null && item.getType() != Material.AIR) {
                data.getArmor().put(slot, ItemData.fromItemStack(item));
            }
        });

        if (kit.getOffHand() != null && kit.getOffHand().getType() != Material.AIR) {
            data.setOffHand(ItemData.fromItemStack(kit.getOffHand()));
        }

        data.setCreated(kit.getCreated());
        data.setModified(kit.getModified());
        return data;
    }

    public CustomKitImpl toCustomKit() {
        final UUID kitId = id != null ? UUID.fromString(id) : UUID.randomUUID();
        final UUID ownerId = owner != null ? UUID.fromString(owner) : UUID.randomUUID();
        final String kitName = name != null ? name : "Custom Kit";
        final ItemStack iconStack = icon != null ? icon.toItemStack(false) : null;

        final CustomKitImpl kit = new CustomKitImpl(
                kitId,
                ownerId,
                kitName,
                description != null ? description : new ArrayList<>(),
                iconStack,
                created > 0 ? created : System.currentTimeMillis(),
                modified > 0 ? modified : System.currentTimeMillis()
        );

        if (items != null) {
            items.forEach((slot, itemData) -> {
                if (itemData != null) {
                    final ItemStack item = itemData.toItemStack(false);
                    if (item != null && item.getType() != Material.AIR) {
                        kit.getItems().put(slot, item);
                    }
                }
            });
        }

        if (armor != null) {
            armor.forEach((slot, itemData) -> {
                if (itemData != null) {
                    final ItemStack item = itemData.toItemStack(false);
                    if (item != null && item.getType() != Material.AIR) {
                        kit.getArmor().put(slot, item);
                    }
                }
            });
        }

        if (offHand != null) {
            final ItemStack offHandStack = offHand.toItemStack(false);
            if (offHandStack != null && offHandStack.getType() != Material.AIR) {
                kit.setOffHand(offHandStack);
            }
        }

        return kit;
    }
}
