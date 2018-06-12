package me.realized.duels.util.compat;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;

public class Collisions extends CompatBase {

    public static void setCollidable(final Player player, final boolean collide) {
        if (COLLIDES_WITH_ENTITIES != null) {
            try {
                COLLIDES_WITH_ENTITIES.set(GET_HANDLE.invoke(player), collide);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        } else {
            player.setCollidable(collide);
        }
    }
}
