package me.realized.duels.commands;

import me.realized.duels.Core;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.data.DataManager;
import me.realized.duels.dueling.DuelManager;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.kits.KitManager;
import org.bukkit.entity.Player;

public abstract class SubCommand {

    private final String name;
    private final String usage;
    private final String permission;
    private final String description;
    private final int length;

    protected final MainConfig config = Core.getInstance().getConfiguration();
    protected final RequestManager requestManager = Core.getInstance().getRequestManager();
    protected final DataManager dataManager = Core.getInstance().getDataManager();
    protected final ArenaManager arenaManager = Core.getInstance().getArenaManager();
    protected final KitManager kitManager = Core.getInstance().getKitManager();
    protected final DuelManager duelManager = Core.getInstance().getDuelManager();

    protected SubCommand(String name, String usage, String permission, String description, int length) {
        this.name = name;
        this.usage = usage;
        this.permission = permission;
        this.description = description;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public int length() {
        return length;
    }

    public abstract void execute(Player sender, String[] args);
}
