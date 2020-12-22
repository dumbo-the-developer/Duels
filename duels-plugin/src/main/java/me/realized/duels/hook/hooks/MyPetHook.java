package me.realized.duels.hook.hooks;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class MyPetHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "MyPet";

    private final Config config;

    public MyPetHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();

        try {
            final Class<?> apiClass = Class.forName("de.Keyle.MyPet.MyPetApi");
            apiClass.getMethod("getMyPetManager");
            final Class<?> managerClass = Class.forName("de.Keyle.MyPet.api.repository.MyPetManager");
            managerClass.getMethod("getMyPet", Player.class);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }
    }

    public void removePet(final Player player) {
        if (!config.isMyPetDespawn()) {
            return;
        }

        final MyPet pet = MyPetApi.getMyPetManager().getMyPet(player);

        if (pet == null) {
            return;
        }

        pet.removePet(false);
    }
}
