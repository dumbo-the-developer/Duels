package me.realized.duels.gui.setting.buttons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Setting;
import me.realized.duels.util.inventory.ItemBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RequestSendButton extends BaseButton {

    public RequestSendButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 5).name("&a&lSEND REQUEST").build());
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingManager.getSafely(player);

        if (setting.getTarget() == null) {
            setting.reset();
            player.closeInventory();
            return;
        }

        final Player target = Bukkit.getPlayer(setting.getTarget());

        if (target == null) {
            setting.reset();
            player.closeInventory();
            lang.sendMessage(player, "ERROR.no-longer-online");
            return;
        }

        player.closeInventory();

        if (!requestManager.send(player, target, setting)) {
            return;
        }

        final String kit = setting.getKit() != null ? setting.getKit().getName() : "Random";
        final String arena = setting.getArena() != null ? setting.getArena().getName() : "Random";
        final int betAmount = setting.getBet();
        final String itemBetting = setting.isItemBetting() ? "&aenabled" : "&cdisabled";

        lang.sendMessage(player, "COMMAND.duel.request.sent.sender",
            "player", target.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.sent.receiver",
            "player", player.getName(), "kit", kit, "arena", arena, "bet_amount", betAmount, "item_betting", itemBetting);

        final String path = "COMMAND.duel.request.sent.clickable-text.";
        final String[] messages = {lang.getMessage(path + "accept"), lang.getMessage(path + "deny")};
        final String[] commands = {"accept " + player.getName(), "deny " + player.getName()};
        final String info = lang.getMessage(path + "info");

        if (info == null) {
            return;
        }

        final List<BaseComponent> allComponents = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(info)));

        for (int i = 0; i < messages.length; i++) {
            final String message = messages[i];

            if (message == null) {
                continue;
            }

            final BaseComponent[] components = TextComponent.fromLegacyText(message);

            for (final BaseComponent component : components) {
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel " + commands[i]));
                allComponents.add(component);
            }
        }

        target.spigot().sendMessage(allComponents.toArray(new BaseComponent[allComponents.size()]));

        final BaseComponent[] components;

        if ((components = TextComponent.fromLegacyText(lang.getMessage(path + "extra"))) != null) {
            target.spigot().sendMessage(components);
        }
    }
}
