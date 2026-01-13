package com.meteordevelopments.duels.util;

import com.meteordevelopments.duels.listeners.ExplosionOwnershipListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class EventUtil {

    private EventUtil() {
    }

    public static Player getDamager(final EntityDamageByEntityEvent event) {
        switch (event.getDamager()) {
            case Player player -> {
                return player;
            }
            case Projectile projectile when projectile.getShooter() instanceof Player -> {
                return (Player) projectile.getShooter();
            }
            case TNTPrimed tnt when tnt.getSource() instanceof Player -> {
                return (Player) tnt.getSource();
            }
            case EnderCrystal crystal -> {
                final java.util.UUID owner = ExplosionOwnershipListener.getCrystalOwner(crystal);
                if (owner != null) {
                    return Bukkit.getPlayer(owner);
                }
            }
            case ExplosiveMinecart minecart -> {
                final java.util.UUID owner = ExplosionOwnershipListener.getTntMinecartOwner(minecart);
                if (owner != null) {
                    return Bukkit.getPlayer(owner);
                }
            }
            default -> {
            }
        }

        return null;
    }
}
