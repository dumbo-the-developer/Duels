package com.meteordevelopments.duels.match.team;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.queue.Queue;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class TeamDuelMatch extends DuelMatch {

    @Getter
    private final Map<Player, Team> playerToTeam = new HashMap<>();
    @Getter
    private final Map<Team, Set<Player>> teamToPlayers = new HashMap<>();
    private final Map<Team, Integer> alivePlayers = new HashMap<>();

    public TeamDuelMatch(final DuelsPlugin plugin, final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        super(plugin, arena, kit, items, bet, source);
    }

    public Set<Team> getAllTeams() {
        return teamToPlayers.keySet();
    }

    public List<String> getTeamNames(final Team team) {
        final Set<Player> members = teamToPlayers.get(team);
        if (members == null) {
            return Collections.emptyList();
        }
        return members.stream().map(Player::getName).collect(Collectors.toList());
    }

    @Override
    public void addPlayer(final Player player) {
        super.addPlayer(player);
        
        // Assign player to a team based on order of joining
        Team team = getOrCreateTeam(player);
        playerToTeam.put(player, team);
        teamToPlayers.computeIfAbsent(team, k -> new HashSet<>()).add(player);
        
        final Integer count = alivePlayers.get(team);
        alivePlayers.put(team, count == null ? 1 : count + 1);
    }

    private Team getOrCreateTeam(final Player player) {
        // Simple team assignment: first half of players go to team 1, second half to team 2
        int totalPlayers = getAllPlayers().size();
        int teamNumber = (totalPlayers <= getSource().getTeamSize()) ? 1 : 2;
        return new Team(teamNumber);
    }

    @Override
    public void markAsDead(final Player player) {
        super.markAsDead(player);
        
        final Team team = playerToTeam.get(player);
        if (team == null) {
            return;
        }
        
        // Set player to spectator mode instead of removing them
        player.setGameMode(GameMode.SPECTATOR);
        
        // Keep player in arena for spectating instead of teleporting to lobby
        // The player will be teleported to lobby when the match actually ends
        
        final Integer count = alivePlayers.get(team);
        if (count == null) {
            return;
        }
        
        alivePlayers.put(team, count - 1);
    }

    @Override
    public int size() {
        return (int) alivePlayers.entrySet().stream().filter(entry -> entry.getValue() > 0).count();
    }

    public boolean isTeamEliminated(final Team team) {
        return alivePlayers.getOrDefault(team, 0) <= 0;
    }

    public Set<Player> getAlivePlayersInTeam(final Team team) {
        return teamToPlayers.getOrDefault(team, Collections.emptySet())
                .stream()
                .filter(player -> !isDead(player))
                .collect(Collectors.toSet());
    }

    public Team getWinningTeam() {
        return alivePlayers.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Set<Player> getWinningTeamPlayers() {
        Team winningTeam = getWinningTeam();
        if (winningTeam == null) {
            return Collections.emptySet();
        }
        return teamToPlayers.getOrDefault(winningTeam, Collections.emptySet());
    }

    public Set<Player> getLosingTeamPlayers() {
        Team winningTeam = getWinningTeam();
        if (winningTeam == null) {
            return Collections.emptySet();
        }
        
        return teamToPlayers.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(winningTeam))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }

    public static class Team {
        private final int teamNumber;
        
        public Team(final int teamNumber) {
            this.teamNumber = teamNumber;
        }
        
        public int getTeamNumber() {
            return teamNumber;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Team team = (Team) obj;
            return teamNumber == team.teamNumber;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(teamNumber);
        }
        
        @Override
        public String toString() {
            return "Team " + teamNumber;
        }
    }
}
