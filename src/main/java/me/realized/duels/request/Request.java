package me.realized.duels.request;

import java.util.UUID;
import lombok.Getter;
import me.realized.duels.cache.Setting;
import org.bukkit.entity.Player;

public class Request {

    @Getter
    private final UUID target;
    @Getter
    private final Setting setting;
    @Getter
    private final long creation;

    Request(final Player target, final Setting setting) {
        this.target = target.getUniqueId();
        // clone settings to prevent weird issues
        this.setting = setting;
        this.creation = System.currentTimeMillis();
    }
}
