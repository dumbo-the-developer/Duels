package me.realized.duels.gui.betting.buttons;

import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.gui.betting.BettingGui;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class StateButton extends BaseButton {

    private final BettingGui gui;
    private final UUID owner;

    public StateButton(final DuelsPlugin plugin, final BettingGui gui, final Player owner) {
        super(plugin, ItemBuilder.of(Material.INK_SACK, 1, (short) 8).name("&7&lNOT READY").build());
        this.gui = gui;
        this.owner = owner.getUniqueId();
    }

    @Override
    public void onClick(final Player player) {
        if (!gui.isReady(player) && player.getUniqueId().equals(owner)) {
            getDisplayed().setDurability((short) 10);
            setDisplayName("&a&lREADY");
            gui.update(player, this);
            gui.setReady(player);
        }
    }
}
