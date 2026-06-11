package com.meteordevelopments.duelsffa.selection;

import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.config.Lang;
import com.meteordevelopments.duelsffa.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Lang lang;
    private final Map<UUID, Selection> selections = new HashMap<>();

    public SelectionManager(final FfaExtension extension) {
        this.lang = extension.getLang();
    }

    public Selection get(final Player player) {
        return selections.get(player.getUniqueId());
    }

    public Selection getOrCreate(final Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), result -> new Selection());
    }

    public void setFirst(final Player player, final Location location) {
        Selection selection = getOrCreate(player);
        selection.setFirst(location);
        lang.sendMessage(player, "COMMAND.ffa.pos-set", "pos", "First", "location", StringUtil.from(location));
    }

    public void setSecond(final Player player, final Location location) {
        Selection selection = getOrCreate(player);
        selection.setSecond(location);
        lang.sendMessage(player, "COMMAND.ffa.pos-set", "pos", "Second", "location", StringUtil.from(location));
    }

    public void clear(final Player player) {
        selections.remove(player.getUniqueId());
    }
}
