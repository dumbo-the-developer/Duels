package com.meteordevelopments.duels.gui.bind.buttons;

import lombok.Setter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.bind.BindGui;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class BindButton extends BaseButton {

    private final KitImpl kit;
    private final ArenaImpl arena;
    @Setter
    private BindGui gui;

    public BindButton(final DuelsPlugin plugin, final KitImpl kit, final ArenaImpl arena) {
        super(plugin, ItemBuilder.of(Items.EMPTY_MAP).build());
        this.kit = kit;
        this.arena = arena;
        setDisplayName(plugin.getLang().getMessage("GUI.bind.buttons.arena.name", "arena", arena.getName()), plugin.getLang());
        update();
    }

    private void update() {
        final boolean state = arena.isBound(kit);
        setGlow(state);

        String kits = StringUtil.join(arena.getKits().stream().map(KitImpl::getName).collect(Collectors.toList()), ", ");
        kits = kits.isEmpty() ? lang.getMessage("GENERAL.none") : kits;
        setLore(lang, lang.getMessage("GUI.bind.buttons.arena.lore-" + (state ? "bound" : "not-bound"), "kits", kits).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        arena.bind(kit);
        update();
        gui.calculatePages();
    }
}
