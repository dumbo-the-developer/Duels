package me.realized.duels.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.util.Log;
import org.bukkit.inventory.ItemStack;

public class PlayerData {

    private static transient final String ITEM_LOAD_FAILURE = "Could not load item %s!";

    public static PlayerData fromPlayerInfo(final PlayerInfo info) {
        return new PlayerData(info);
    }

    private Map<String, Map<Integer, ItemData>> items = new HashMap<>();
    private Collection<PotionEffectData> effects = new ArrayList<>();
    private double health;
    private int hunger;
    private LocationData location;
    private List<ItemData> extra = new ArrayList<>();

    private PlayerData() {}

    private PlayerData(final PlayerInfo info) {
        this.health = info.getHealth();
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

    public PlayerInfo toPlayerInfo() {
        final PlayerInfo info = new PlayerInfo(
            effects.stream().map(PotionEffectData::toPotionEffect).filter(Objects::nonNull).collect(Collectors.toList()),
            health,
            hunger,
            location.toLocation()
        );

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> {
                final ItemStack item = itemData.toItemStack(false);

                if (item == null) {
                    Log.warn(String.format(ITEM_LOAD_FAILURE, itemData.toString()));
                    return;
                }

                data.put(slot, item);
            }));
            info.getItems().put(entry.getKey(), data);
        }

        info.getExtra().addAll(extra.stream().map(data -> data.toItemStack(false)).filter(Objects::nonNull).collect(Collectors.toList()));
        return info;
    }
}
