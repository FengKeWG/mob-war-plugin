package org.windguest.mobwar;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.windguest.mobwar.events.EventsMain;
import org.windguest.mobwar.games.Jobs;
import org.windguest.mobwar.games.Players;

public class Placeholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getAuthor() {
        return "WindGuest";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MobWar";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.startsWith("kills_")) {
            if (identifier.equals("kills_总")) {
                int kills = Players.getPlayerKills(player, "总");
                return String.valueOf(kills);
            } else if (identifier.equals("kills_current")) {
                String jobName = Players.getJobFromPlayer(player);
                if (jobName != null) {
                    int kills = Players.getPlayerKills(player, jobName);
                    return String.valueOf(kills);
                } else {
                    return "?";
                }
            } else {
                String jobName = identifier.substring("kills_".length());
                int kills = Players.getPlayerKills(player, jobName);
                return String.valueOf(kills);
            }
        }
        if (identifier.startsWith("deaths_")) {
            if (identifier.equals("deaths_总")) {
                int deaths = Players.getPlayerDeaths(player, "总");
                return String.valueOf(deaths);
            } else if (identifier.equals("deaths_current")) {
                String jobName = Players.getJobFromPlayer(player);
                if (jobName != null) {
                    int deaths = Players.getPlayerDeaths(player, jobName);
                    return String.valueOf(deaths);
                } else {
                    return "?";
                }
            } else {
                String jobName = identifier.substring("deaths_".length());
                int deaths = Players.getPlayerDeaths(player, jobName);
                return String.valueOf(deaths);
            }
        }
        if (identifier.startsWith("kdr_")) {
            if (identifier.equals("kdr_总")) {
                double kdr = Players.getPlayerKDRatio(player, "总");
                return String.format("%.2f", kdr);
            } else if (identifier.equals("kdr_current")) {
                String jobName = Players.getJobFromPlayer(player);
                if (jobName != null) {
                    double kdr = Players.getPlayerKDRatio(player, jobName);
                    return String.format("%.2f", kdr);
                } else {
                    return "?";
                }
            } else {
                String jobName = identifier.substring("kdr_".length());
                double kdr = Players.getPlayerKDRatio(player, jobName);
                return String.format("%.2f", kdr);
            }
        }
        if (identifier.startsWith("maxStreak_")) {
            if (identifier.equals("maxStreak_current")) {
                String jobName = Players.getJobFromPlayer(player);
                if (jobName != null) {
                    int maxStreak = Players.getPlayerMaxStreaks(player, jobName);
                    return String.valueOf(maxStreak);
                } else {
                    return "?";
                }
            } else if (identifier.equals("maxStreak_总")) {
                int maxStreak = Players.getPlayerTotalMaxStreaks(player);
                return String.valueOf(maxStreak);
            } else {
                String jobName = identifier.substring("maxStreak_".length());
                int maxStreak = Players.getPlayerMaxStreaks(player, jobName);
                return String.valueOf(maxStreak);
            }
        }
        switch (identifier) {
            case "streak":
                return String.valueOf(Players.getPlayerStreaks(player));
            case "job":
                return Players.getJobFromPlayer(player);
            case "color":
                return Jobs.getJobColorCode(player);
            case "prefix":
                return Players.getPlayerPrefix(player);
            case "team_color":
                return Players.getTeamColor(player);
            case "current_event":
                return EventsMain.getEventName();
            case "time_remaining":
                int seconds = EventsMain.getEventTimeRemaining();
                int minutes = seconds / 60;
                return minutes + "分" + seconds % 60 + "秒";
            case "key_person":
                return EventsMain.getKeyMessage();
            case "key_coordinates":
                return EventsMain.getKeyPersonCoordinates();
            case "health":
                int health = (int) player.getHealth();
                return String.valueOf(health);
            case "stars":
                return String.valueOf(Players.getStar(player));
        }
        return "🚫";
    }
}
