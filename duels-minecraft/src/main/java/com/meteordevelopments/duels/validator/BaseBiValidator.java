package com.meteordevelopments.duels.validator;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.party.PartyManagerImpl;
import com.meteordevelopments.duels.request.RequestManager;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.util.validator.BiValidator;

public abstract class BaseBiValidator<T1, T2> implements BiValidator<T1, T2> {

    protected final DuelsPlugin plugin;

    protected final Config config;
    protected final Lang lang;
    protected final UserManagerImpl userManager;
    protected final PartyManagerImpl partyManager;
    protected final ArenaManagerImpl arenaManager;
    protected final SpectateManagerImpl spectateManager;
    protected final RequestManager requestManager;

    public BaseBiValidator(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.partyManager = plugin.getPartyManager();
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();
        this.requestManager = plugin.getRequestManager();
    }

    @Override
    public boolean shouldValidate() {
        return true;
    }
}
