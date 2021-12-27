package me.realized.duels.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class EventUtil {

    public static Player getDamager(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            return (Player) ((Projectile) event.getDamager()).getShooter();
        }

        return null;
    }

    private EventUtil() {}
}
