package com.meteordevelopments.duels.hook.hooks;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.api.MCPetsAPI;
import fr.nocsy.mcpets.data.PetDespawnReason;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;

public class MCPetsHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "MCPets";

    private final Config config;

    public MCPetsHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();

        try {
            Class.forName("fr.nocsy.mcpets.api.MCPetsAPI");
            Class.forName("fr.nocsy.mcpets.data.Pet");
            Class.forName("fr.nocsy.mcpets.data.PetDespawnReason");

            MCPetsAPI.class.getMethod("getActivePetsForPlayer", java.util.UUID.class);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }
    }

    public void removePets(final Player player) {
        if (!config.isMcPetsDespawn()) {
            return;
        }

        final List<Pet> pets = MCPetsAPI.getActivePetsForPlayer(player.getUniqueId());

        if (pets.isEmpty()) return;

        for (Pet pet : new ArrayList<>(pets)) {
            if (pet == null) continue;
            pet.despawn(PetDespawnReason.PETDESPAWN_SKILL);
        }
    }

}
