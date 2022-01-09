package me.realized.duels.util.inventory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class ItemUtil {

    public static ItemStack itemFrom64(final String data) {
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            try (final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private ItemUtil() {}
}
