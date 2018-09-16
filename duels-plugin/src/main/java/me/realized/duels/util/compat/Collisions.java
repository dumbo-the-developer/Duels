package me.realized.duels.util.compat;

import java.lang.reflect.InvocationTargetException;
import me.realized.duels.util.Log;
import org.bukkit.entity.Player;

public class Collisions extends CompatBase {

    public static void setCollidable(final Player player, final boolean collide) {
        if (CompatUtil.hasSetCollidable()) {
            player.setCollidable(collide);
        } else if (COLLIDES_WITH_ENTITIES != null) {
            try {
                COLLIDES_WITH_ENTITIES.set(GET_HANDLE.invoke(player), collide);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        } else {
            Log.error("Failed to execute Collisions#setCollidable, please contact the developer.");
        }
    }
}
