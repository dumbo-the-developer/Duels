package com.meteordevelopments.duels.command.commands.duel.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.customkit.CustomKitSnapshot;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.core.request.RequestImpl;
import com.meteordevelopments.duels.gui.bedrock.BedrockCustomKitForm;
import com.meteordevelopments.duels.gui.customkit.CustomKitPreviewGui;
import com.meteordevelopments.duels.util.FloodgateUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PreviewCommand extends BaseCommand {

    public PreviewCommand(final DuelsPlugin plugin) {
        super(plugin, "preview", "preview [player]", "Previews the kit of a duel request.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final RequestImpl request = plugin.getRequestManager().get(target, player);
        if (request == null) {
            lang.sendMessage(player, "ERROR.duel.no-request", "name", target.getName());
            return;
        }

        final CustomKitSnapshot snapshot = request.getSettings().getCustomKitSnapshot();
        if (snapshot != null) {
            if (FloodgateUtil.isBedrockPlayer(player)) {
                BedrockCustomKitForm.openPreview(plugin, player, snapshot, null);
            } else {
                CustomKitPreviewGui.open(plugin, player, snapshot, null);
            }
            return;
        }

        if (request.getKit() != null) {
            lang.sendMessage(player, "COMMAND.duel.preview.server-kit", "kit", request.getKit().getName());
            return;
        }

        lang.sendMessage(player, "COMMAND.duel.preview.own-inventory");
    }
}
