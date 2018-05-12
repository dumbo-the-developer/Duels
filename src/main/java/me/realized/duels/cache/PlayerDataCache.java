package me.realized.duels.cache;

import org.bukkit.entity.Player;

public class PlayerDataCache extends Cache<PlayerData> {

    @Override
    PlayerData create(final Player player) {
        return new PlayerData();
    }
}
