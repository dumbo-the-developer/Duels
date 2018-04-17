package me.realized.duels.cache;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.arena.Arena;
import me.realized.duels.gui.setting.SettingGui;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.gui.SinglePageGui;

public class Setting {

    @Getter
    @Setter
    private UUID target;
    @Getter
    @Setter
    private String targetName;
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;
    @Getter
    @Setter
    private double bet;
    @Getter
    @Setter
    private boolean itemBetting;
    @Getter
    @Setter
    private SettingGui gui;

    public void reset() {
        target = null;
        targetName = null;
        kit = null;
        arena = null;
        bet = 0;
        itemBetting = false;
    }
}
