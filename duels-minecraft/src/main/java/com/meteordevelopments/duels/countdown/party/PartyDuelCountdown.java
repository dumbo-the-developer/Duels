package com.meteordevelopments.duels.countdown.party;

import java.util.HashMap;
import java.util.Map;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.countdown.DuelCountdown;
import com.meteordevelopments.duels.match.party.PartyDuelMatch;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Titles;

public class PartyDuelCountdown extends DuelCountdown {

    private final PartyDuelMatch match;

    private final Map<Party, String> info = new HashMap<>();

    public PartyDuelCountdown(final DuelsPlugin plugin, final ArenaImpl arena, final PartyDuelMatch match) {
        super(plugin, arena, match, plugin.getConfiguration().getCdPartyDuelMessages(), plugin.getConfiguration().getCdPartyDuelTitles());
        this.match = match;
        match.getAllParties().forEach(party -> info.put(party, StringUtil.join(match.getNames(party), ", ")));
    }
    
    @Override
    protected void sendMessage(final String rawMessage, final String message, final String title) {
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
        match.getPlayerToParty().forEach((player, value) -> {
            config.playSound(player, rawMessage);
            player.sendMessage(message
                    .replace("%opponents%", info.get(arena.getOpponent(value)))
                    .replace("%kit%", kitName)
                    .replace("%arena%", arena.getName())
            );

            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        });
    }
}
