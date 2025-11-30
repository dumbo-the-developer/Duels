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
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            return (Player) ((Projectile) event.getDamager()).getShooter();
        } else if (event.getDamager() instanceof TNTPrimed tnt && tnt.getSource() instanceof Player) {
            return (Player) tnt.getSource();
        } else if (event.getDamager() instanceof EnderCrystal crystal) {
            final java.util.UUID owner = ExplosionOwnershipListener.getCrystalOwner(crystal);
            if (owner != null) {
                return Bukkit.getPlayer(owner);
            }
        } else if (event.getDamager() instanceof ExplosiveMinecart minecart) {
            final java.util.UUID owner = ExplosionOwnershipListener.getTntMinecartOwner(minecart);
            if (owner != null) {
                return Bukkit.getPlayer(owner);
            }
        }

        return null;
    }
}
