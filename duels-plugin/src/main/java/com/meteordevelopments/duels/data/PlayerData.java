package com.meteordevelopments.duels.data;

import com.meteordevelopments.duels.player.PlayerInfo;
import com.meteordevelopments.duels.util.Log;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {

    private static final String ITEM_LOAD_FAILURE = "Could not load item %s!";
    private final Map<String, Map<Integer, ItemData>> items = new HashMap<>();
    private final Collection<PotionEffectData> effects = new ArrayList<>();
    private double health;
    private float experience;
    private int level;
    private int hunger;
    private LocationData location;
    private final List<ItemData> extra = new ArrayList<>();
    private PlayerData() {
    }

    private PlayerData(final PlayerInfo info) {
        this.health = info.getHealth();
        this.experience = info.getExperience();
        this.level = info.getLevel();
        this.hunger = info.getHunger();
        this.location = LocationData.fromLocation(info.getLocation());

        for (final Map.Entry<String, Map<Integer, ItemStack>> entry : info.getItems().entrySet()) {
            final Map<Integer, ItemData> data = new HashMap<>();
            entry.getValue().entrySet()
                    .stream()
                    .filter(value -> Objects.nonNull(value.getValue()))
                    .forEach(value -> data.put(value.getKey(), ItemData.fromItemStack(value.getValue())));
            items.put(entry.getKey(), data);
        }

        info.getExtra().forEach(item -> extra.add(ItemData.fromItemStack(item)));
    }

    public static PlayerData fromPlayerInfo(final PlayerInfo info) {
        return new PlayerData(info);
    }

    public PlayerInfo toPlayerInfo() {
        final PlayerInfo info = new PlayerInfo(
                effects.stream().map(PotionEffectData::toPotionEffect).filter(Objects::nonNull).collect(Collectors.toList()),
                health,
                experience,
                level,
                hunger,
                location.toLocation()
        );

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> {
                final ItemStack item = itemData.toItemStack(false);

                if (item == null) {
                    Log.warn(String.format(ITEM_LOAD_FAILURE, itemData));
                    return;
                }

                data.put(slot, item);
            }));
            info.getItems().put(entry.getKey(), data);
        }

        info.getExtra().addAll(extra.stream().map(data -> data.toItemStack(false)).filter(Objects::nonNull).toList());
        return info;
    }
}
