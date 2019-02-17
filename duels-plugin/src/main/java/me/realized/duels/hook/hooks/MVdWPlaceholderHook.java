package me.realized.duels.hook.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManager;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class MVdWPlaceholderHook extends PluginHook<DuelsPlugin> {

    private final UserManager userDataManager;

    public MVdWPlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, "MVdWPlaceholderAPI");
        this.userDataManager = plugin.getUserManager();

        final Placeholders placeholders = new Placeholders();
        PlaceholderAPI.registerPlaceholder(plugin, "duels_wins", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_losses", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "duels_can_request", placeholders);
    }

    public class Placeholders implements PlaceholderReplacer {

        @Override
        public String onPlaceholderReplace(final PlaceholderReplaceEvent event) {
            final Player player = event.getPlayer();

            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player);

            if (user == null) {
                return null;
            }

            switch (event.getPlaceholder()) {
                case "duels_wins":
                    return String.valueOf(user.getWins());
                case "duels_losses":
                    return String.valueOf(user.getLosses());
                case "duels_can_request":
                    return String.valueOf(user.canRequest());
            }

            return null;
        }
    }
}
