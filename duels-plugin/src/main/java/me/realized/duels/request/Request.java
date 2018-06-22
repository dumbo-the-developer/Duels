package me.realized.duels.request;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.setting.Settings;
import org.bukkit.entity.Player;

public class Request implements me.realized.duels.api.request.Request {

    @Getter
    private final UUID sender;
    @Getter
    private final UUID target;
    @Getter
    private final Settings settings;
    @Getter
    private final long creation;

    Request(final Player sender, final Player target, final Settings setting) {
        this.sender = sender.getUniqueId();
        this.target = target.getUniqueId();
        this.settings = setting.lightCopy();
        this.creation = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public Kit getKit() {
        return settings.getKit();
    }

    @Nullable
    @Override
    public Arena getArena() {
        return settings.getArena();
    }

    @Override
    public boolean canBetItems() {
        return settings.isItemBetting();
    }

    @Override
    public int getBet() {
        return settings.getBet();
    }
}
