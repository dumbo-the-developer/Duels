package me.realized.duels.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.User;
import org.bukkit.entity.Player;

public class UserData implements User {

    @Getter
    private final UUID uuid;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int wins;
    @Getter
    @Setter
    private int losses;
    @Setter
    private boolean requests = true;
    @Getter
    private List<MatchData> matches = new ArrayList<>();

    public UserData(final Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }

    public void addMatch(final MatchData matchData) {
        matches.add(matchData);
    }

    public boolean canRequest() {
        return requests;
    }

    @Override
    public String toString() {
        return "UserData{" +
            "uuid=" + uuid +
            ", name='" + name + '\'' +
            ", wins=" + wins +
            ", losses=" + losses +
            ", requests=" + requests +
            ", matches=" + matches +
            '}';
    }
}
