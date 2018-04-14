package me.realized.duels.cache;

import org.bukkit.entity.Player;

public class SettingCache extends Cache<Setting> {

    @Override
    protected Setting instance(final Player player) {
        return new Setting();
    }
}
