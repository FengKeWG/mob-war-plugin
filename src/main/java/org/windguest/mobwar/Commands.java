package org.windguest.mobwar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.windguest.mobwar.Games.Edit;
import org.windguest.mobwar.Games.Jobs;
import org.windguest.mobwar.Games.Players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {

    private final List<String> allCommands = Arrays.asList("save", "reset", "stars", "edit", "clear", "clearall", "job", "jobnotp");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
                case "clear":
                    Edit.clearSingleKits(args[1]);
                    break;
                case "clearall":
                    Edit.clearAllKits();
                    break;
                case "job":
                    Jobs.startGame(player, args[1], true);
                    break;
                case "jobnotp":
                    Jobs.startGame(player, args[1], false);
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }
        List<String> completions = new ArrayList<>(allCommands);
        String partial = args[0].toLowerCase();
        completions.removeIf(cmd -> !cmd.toLowerCase().startsWith(partial));

        return completions;
    }
}