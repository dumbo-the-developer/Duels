package me.realized._duels.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import java.util.Arrays;
import java.util.List;
import me.realized._duels.Core;
import me.realized._duels.data.UserData;
import org.bukkit.entity.Player;

public class MVdWPlaceholderHook extends PluginHook implements PlaceholderReplacer {

    private static final List<String> PLACEHOLDERS = Arrays.asList("duels_wins", "duels_losses", "duels_request_enabled");

    private final Core instance;

    public MVdWPlaceholderHook(Core instance) {
        super("MVdWPlaceholderAPI");
        this.instance = instance;

        if (isEnabled()) {
            for (String placeholder : PLACEHOLDERS) {
                PlaceholderAPI.registerPlaceholder(instance, placeholder, this);
            }
        }
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return "Player is required.";
        }

        UserData user = instance.getDataManager().getUser(player.getUniqueId(), false);

        if (user == null) {
            return "User data not found.";
        }

        switch (event.getPlaceholder()) {
            case "duels_wins":
                return String.valueOf(user.get(UserData.StatsType.WINS));
            case "duels_losses":
                return String.valueOf(user.get(UserData.StatsType.LOSSES));
            case "duels_request_enabled":
                return user.canRequest() ? "enabled" : "disabled";
        }

        return "Invalid placeholder.";
    }
}
