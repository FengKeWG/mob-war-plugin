package org.windguest.mobwar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.windguest.mobwar.Games.Edit;
import org.windguest.mobwar.Games.Jobs;
import org.windguest.mobwar.Games.Players;

public class CommandsManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            switch (args[0]) {
                case "save":
                    Edit.quitEdit(player);
                    break;
                case "reset":
                    Edit.clearPlayerNowEditedKits(player);
                    break;
            }
            if (!player.isOp()) {
                return false;
            }
            switch (args[0]) {
                case "stars":
                    Players.addStar(player, Integer.parseInt(args[1]));
                    break;
                case "edit":
                    Edit.startEdit(player, args[1]);
                    break;
                case "clearall":
                    Edit.clearAllSavedKits(args[1]);
                    break;
                case "job":
                    Jobs.startGame(player, args[1], true);
                    break;
                case "jobnotp":
                    Jobs.startGame(player, args[1], false);
                    break;
                default:
                    player.sendMessage("未知命令!");
                    break;
            }
        }
        return false;
    }
}