package me.realized.duels.setting;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.gui.setting.SettingGui;
import me.realized.duels.kit.Kit;
import org.bukkit.entity.Player;

public class Setting {

    private final DuelsPlugin plugin;

    @Getter
    private UUID target;
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;
    @Getter
    @Setter
    private int bet;
    @Getter
    @Setter
    private boolean itemBetting;
    private SettingGui gui;

    public Setting(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reset() {
        target = null;
        kit = null;
        arena = null;
        bet = 0;
        itemBetting = false;
    }

    public void setTarget(final Player target) {
        if (this.target != null && !this.target.equals(target.getUniqueId())) {
            reset();
        }

        this.target = target.getUniqueId();
    }

    public void updateGui(final Player player) {
        if (gui != null) {
            gui.update(player);
        }
    }

    public void openGui(final Player player) {
        (this.gui != null ? this.gui : (this.gui = plugin.getGuiListener().addGui(player, new SettingGui(plugin)))).open(player);
    }

    // Don't copy the gui since it won't be required to start a match.
    public Setting lightCopy() {
        final Setting copy = new Setting(plugin);
        copy.target = target;
        copy.kit = kit;
        copy.arena = arena;
        copy.bet = bet;
        copy.itemBetting = itemBetting;
        return copy;
    }
}
