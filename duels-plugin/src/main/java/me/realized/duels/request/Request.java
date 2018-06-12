package me.realized.duels.request;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.setting.Setting;
import org.bukkit.entity.Player;

public class Request implements me.realized.duels.api.request.Request {

    @Getter
    private final UUID sender;
    @Getter
    private final UUID target;
    @Getter
    private final Setting setting;
    @Getter
    private final long creation;

    Request(final Player sender, final Player target, final Setting setting) {
        this.sender = sender.getUniqueId();
        this.target = target.getUniqueId();
        this.setting = setting.lightCopy();
        this.creation = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public Kit getKit() {
        return setting.getKit();
    }

    @Nullable
    @Override
    public Arena getArena() {
        return setting.getArena();
    }

    @Override
    public boolean canBetItems() {
        return setting.isItemBetting();
    }

    @Override
    public int getBet() {
        return setting.getBet();
    }
}
