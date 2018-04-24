package me.realized.duels.gui.setting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.request.RequestManager;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestSendButton extends Button {

    private final SettingCache settingCache;
    private final RequestManager requestManager;

    public RequestSendButton(final DuelsPlugin plugin) {
        super(ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name("&a&lSEND REQUEST").build());
        this.settingCache = plugin.getSettingCache();
        this.requestManager = plugin.getRequestManager();
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingCache.get(player);

        if (setting.getTarget() == null) {
            player.closeInventory();
            return;
        }

        final Player target = Bukkit.getPlayer(setting.getTarget());

        if (target == null) {
            setting.reset();
            player.sendMessage("Target is no longer online");
            player.closeInventory();
            return;
        }

        player.closeInventory();
        player.sendMessage("Sent request to " + target.getName());
        target.sendMessage("Received a request from " + player.getName());
        requestManager.send(player, target, setting);
    }
}
