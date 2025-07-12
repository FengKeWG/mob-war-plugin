package org.windguest.mobwar.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.Disguise;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

import java.util.*;
import java.util.stream.Collectors;

public class Team {

    private static int redPlayers = 0;
    private static int bluePlayers = 0;
    private static int redStars = 0;
    private static int blueStars = 0;
    private static final Map<UUID, String> teamAssignment = new HashMap<>();
    private static final Map<String, Integer> teamKills = new HashMap<>();
    private static final Map<UUID, Integer> teamEventKills = new HashMap<>();
    private static final int eventDuration = 600;
    private static final Main plugin = Main.getInstance();

    public static void startEvent() {
        Bukkit.broadcastMessage("§b[🎮] 团队事件开始！玩家被随机分配到红队或蓝队！");
        for (Player player : Bukkit.getOnlinePlayers()) {
            assignTeam(player);
            player.sendTitle("§c团队事件开始！", ChatColor.GREEN + "玩家被随机分配到红队或蓝队！", 10, 70, 20);
        }
        EventsMain.setEvent("团队", eventDuration);
        teamKills.put("红队", 0);
        teamKills.put("蓝队", 0);
        new BukkitRunnable() {

            public void run() {
                if (!EventsMain.isEventActive()) {
                    cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String team = teamAssignment.get(player.getUniqueId());
                    if (team != null) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, true, false, false));
                        continue;
                    }
                    assignTeam(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void endEvent() {
        sendTeamLeaderboard();
        teamAssignment.clear();
        teamEventKills.clear();
        teamKills.put("红队", 0);
        teamKills.put("蓝队", 0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.GLOWING);
            player.setPlayerListName(player.getName());
            Disguise.cancelGlow(player);
        }
    }

    public static void assignTeam(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (teamAssignment.containsKey(playerUUID)) {
            return;
        }
        String team;
        if (redPlayers < bluePlayers) {
            team = "红队";
            teamAssignment.put(playerUUID, "红队");
            redPlayers++;
            redStars += Players.getStar(player);
        } else if (bluePlayers < redPlayers) {
            team = "蓝队";
            teamAssignment.put(playerUUID, "蓝队");
            bluePlayers++;
            blueStars += Players.getStar(player);
        } else if (redStars < blueStars) {
            team = "红队";
            teamAssignment.put(playerUUID, "红队");
            redPlayers++;
            redStars += Players.getStar(player);
        } else {
            team = "蓝队";
            teamAssignment.put(playerUUID, "蓝队");
            bluePlayers++;
            blueStars += Players.getStar(player);
        }
        player.sendMessage("§e你被分配到 " + (team.equals("红队") ? "§c" : "§9") + team);
        if (team.equals("红队")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false, false));
            player.setPlayerListName("§c" + player.getName());
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false, false));
            player.setPlayerListName("§9" + player.getName());
        }
    }

    public static String getTeam(Player player) {
        return teamAssignment.get(player.getUniqueId());
    }

    public static void addKill(Player player) {
        String team = teamAssignment.get(player.getUniqueId());
        int kills = teamKills.getOrDefault(team, 0);
        teamKills.put(team, kills + 1);
        kills = teamEventKills.getOrDefault(player.getUniqueId(), 0);
        teamEventKills.put(player.getUniqueId(), kills + 1);
    }

    public static int getTeamKills(String team) {
        return teamKills.get(team);
    }

    public static void sendTeamLeaderboard() {
        int redTeamKills = teamKills.getOrDefault("红队", 0);
        int blueTeamKills = teamKills.getOrDefault("蓝队", 0);
        String winningTeam;
        boolean isTie;
        if (redTeamKills > blueTeamKills) {
            winningTeam = "红队";
            isTie = false;
        } else if (blueTeamKills > redTeamKills) {
            winningTeam = "蓝队";
            isTie = false;
        } else {
            winningTeam = "平局";
            isTie = true;
        }
        String winningTeamColor = "§e";
        if (!isTie) {
            winningTeamColor = winningTeam.equals("红队") ? "§c" : "§9";
        }
        if (!isTie) {
            Bukkit.broadcastMessage("§e[🏆] 团队事件排行榜 - " + winningTeamColor + winningTeam + "获胜！");
        } else {
            Bukkit.broadcastMessage("§e[🏆] 团队事件排行榜 - " + winningTeamColor + "平局！");
        }
        String subtitle;
        if (!isTie) {
            subtitle = winningTeamColor + winningTeam + "获胜！";
        } else {
            subtitle = "§e平局！";
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("", subtitle, 10, 70, 20);
        }
        List<Map.Entry<UUID, Integer>> sortedTeamKills;
        if (!isTie) {
            sortedTeamKills = teamEventKills.entrySet().stream()
                    .filter(entry -> winningTeam.equals(teamAssignment.get(entry.getKey())))
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } else {
            sortedTeamKills = teamEventKills.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }
        for (int i = 0; i < sortedTeamKills.size(); ++i) {
            Map.Entry<UUID, Integer> entry = sortedTeamKills.get(i);
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            String color;
            String position;
            switch (i) {
                case 0: {
                    position = "🥇";
                    color = "§6";
                    break;
                }
                case 1: {
                    position = "🥈";
                    color = "§9";
                    break;
                }
                case 2: {
                    position = "🥉";
                    color = "§2";
                    break;
                }
                default: {
                    position = String.valueOf(i + 1);
                    color = (i < 5) ? "§f" : "§7";
                }
            }
            int reward = EventsMain.getReward(i);
            EventsMain.rewardTopPlayers(player, reward);
            Bukkit.broadcastMessage(color + "[" + position + "] " + player.getName() + " §c" + entry.getValue() + "🗡" +
                    (reward > 0 ? " §f[+" + reward + "§b✦§f]" : ""));
        }
    }
}
