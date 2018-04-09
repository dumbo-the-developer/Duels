package me.realized._duels.data;

import me.realized._duels.utilities.Reloadable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager implements Reloadable {

    private final Map<UUID, PlayerData> data = new HashMap<>();

    public void setData(Player player) {
        data.put(player.getUniqueId(), new PlayerData(player));
    }

    public PlayerData getData(Player player) {
        return data.get(player.getUniqueId());
    }

    public void removeData(Player player) {
        data.remove(player.getUniqueId());
    }

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            data.clear();
        }
    }
}
