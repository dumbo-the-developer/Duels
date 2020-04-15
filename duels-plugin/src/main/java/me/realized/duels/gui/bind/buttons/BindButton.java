package me.realized.duels.gui.bind.buttons;

import java.util.stream.Collectors;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.gui.bind.BindGui;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public class BindButton extends BaseButton {

    @Setter
    private BindGui gui;
    private final Kit kit;
    private final Arena arena;

    public BindButton(final DuelsPlugin plugin, final Kit kit, final Arena arena) {
        super(plugin, ItemBuilder.of(Items.EMPTY_MAP).build());
        this.kit = kit;
        this.arena = arena;
        setDisplayName(plugin.getLang().getMessage("GUI.bind.buttons.arena.name", "arena", arena.getName()));
        update();
    }

    private void update() {
        final boolean state = arena.isBound(kit);
        setGlow(state);

        String kits = StringUtils.join(arena.getKits().stream().map(Kit::getName).collect(Collectors.toList()), ", ");
        kits = kits.isEmpty() ? lang.getMessage("GENERAL.none") : kits;
        setLore(lang.getMessage("GUI.bind.buttons.arena.lore-" + (state ? "bound" : "not-bound"), "kits", kits).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        arena.bind(kit);
        update();
        gui.calculatePages();
    }
}
