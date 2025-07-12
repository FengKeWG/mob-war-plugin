package org.windguest.mobwar.events;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

public class EventsMain {
    private static String currentEventName = "无事件";
    private static int currentEventTimeRemaining = 0;
    private static boolean isEventActive = false;
    private static boolean isPaused = false;
    private static int eventIndex = 0;
    private static final String[] eventSequence = new String[]{"团队", "无事件", "空岛", "无事件", "刷怪笼", "无事件", "末地", "无事件", "疯狂", "无事件"};
    private static final int noEventDuration = 300;
    static Main plugin = Main.getInstance();

    public static void startEvents() {
        new BukkitRunnable() {
            public void run() {
                if (Bukkit.getOnlinePlayers().size() < 2) {
                    if (isEventActive) {
                        endEvent();
                    }
                    currentEventName = "无事件";
                    isPaused = true;
                } else if (isPaused) {
                    isPaused = false;
                }
                if (!isPaused) {
                    currentEventTimeRemaining--;
                }
                if (currentEventTimeRemaining <= 0) {
                    if (!isEventActive) {
                        startNextEvent();
                    } else {
                        endEvent();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private static void startNextEvent() {
        String nextEvent = eventSequence[eventIndex];
        eventIndex = (eventIndex + 1) % eventSequence.length;
        switch (nextEvent) {
            case "末地":
                End.startEvent();
                break;
            case "末影龙":
                Dragon.startEvent();
                break;
            case "疯狂":
                Crazy.startEvent();
                break;
            case "刷怪笼":
                Spawner.startEvent();
                break;
            case "空岛":
                Sky.startEvent();
                break;
            case "团队":
                Team.startEvent();
                break;
            case "无事件":
                startNoEvent();
                break;
        }
    }

    private static void startNoEvent() {
        currentEventName = "无事件";
        currentEventTimeRemaining = noEventDuration;
        isEventActive = false;
    }

    public static void playDragonRoar() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
    }

    private static void endEvent() {
        switch (currentEventName) {
            case "末影龙":
                Dragon.endEvent();
                break;
            case "末地":
                End.endEvent();
                break;
            case "疯狂":
                Crazy.endEvent();
                break;
            case "刷怪笼":
                Spawner.endEvent();
                break;
            case "空岛":
                Sky.endEvent();
                break;
            case "团队": {
                Team.endEvent();
                break;
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setMaxHealth(20.0);
            player.setHealth(20.0);
        }
        currentEventName = "无事件";
        currentEventTimeRemaining = noEventDuration;
        isEventActive = false;
        isPaused = false;
        startNextEvent();
    }

    public static String getKeyMessage() {
        switch (currentEventName) {
            case "末影龙":
                if (Dragon.getDragonPlayer() != null) {
                    Player player = Dragon.getDragonPlayer();
                    int dragonTime = Dragon.getDragonTime(player);
                    return player.getName() + " &c[" + dragonTime + "秒]";
                }
                return "无";
            case "末地":
                Player highestHealthPlayer = Players.getHighestMaxHealthPlayer();
                if (highestHealthPlayer != null) {
                    return highestHealthPlayer.getName() + " &c[" + (int) highestHealthPlayer.getMaxHealth() + "❤]";
                }
                return "无";
            case "疯狂": {
                Player topKiller = Crazy.getTopKiller();
                if (topKiller != null) {
                    return topKiller.getName() + " &c[" + Crazy.getMaxKills() + "🗡]";
                }
                return "无";
            }
            case "刷怪笼": {
                Player topKiller = Spawner.getTopKiller();
                if (topKiller != null) {
                    return topKiller.getName() + " &c[" + Spawner.getMaxKills() + "👾]";
                }
                return "无";
            }
            case "空岛":
                Player topSkyPlayer = Sky.getTopSkyPlayer();
                if (topSkyPlayer != null) {
                    return topSkyPlayer.getName() + " &c[" + Sky.getTopSkyPlayerTime() + "秒]";
                }
                return "无";
            case "团队":
                return "&c" + Team.getTeamKills("红队") + "🗡 &7/ &9" + Team.getTeamKills("蓝队") + "🗡";
        }
        return "无";
    }

    public static String getKeyPersonCoordinates() {
        Player keyPerson = switch (currentEventName) {
            case "末影龙" -> Dragon.getDragonPlayer();
            case "末地" -> Players.getHighestMaxHealthPlayer();
            case "疯狂" -> Crazy.getTopKiller();
            case "刷怪笼" -> Spawner.getTopKiller();
            case "空岛" -> Sky.getTopSkyPlayer();
            default -> null;
        };
        if (keyPerson != null) {
            Location loc = keyPerson.getLocation();
            return "[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
        }
        return "无";
    }

    public static String getEventName() {
        return currentEventName;
    }

    public static int getEventTimeRemaining() {
        return currentEventTimeRemaining;
    }

    public static boolean isEventActive() {
        return isEventActive;
    }

    public static void setEvent(String eventName, int eventTimeRemaining) {
        currentEventName = eventName;
        currentEventTimeRemaining = eventTimeRemaining;
        isEventActive = true;
    }

    public static void sendLeaderboard(Map<UUID, Integer> map, String eventName, String icon) {
        Bukkit.broadcastMessage("§e[🏆] " + eventName + "事件排行榜");
        if (map.isEmpty()) {
            Bukkit.broadcastMessage("§7无");
            return;
        }
        List<Map.Entry<UUID, Integer>> sortedKills = map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .toList();
        for (int i = 0; i < sortedKills.size(); ++i) {
            Map.Entry<UUID, Integer> entry = sortedKills.get(i);
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
            int reward = getReward(i);
            rewardTopPlayers(player, reward);
            Bukkit.broadcastMessage(color + "[" + position + "] " + player.getName() + " §c" + entry.getValue() + icon +
                    (reward > 0 ? " §f[+" + reward + "§b✦§f]" : ""));
        }
    }

    public static int getReward(int position) {
        return switch (position) {
            case 0 -> 50;
            case 1 -> 40;
            case 2 -> 30;
            case 3 -> 20;
            case 4 -> 10;
            default -> 0;
        };
    }

    public static void rewardTopPlayers(Player player, int reward) {
        if (player == null)
            return;
        Players.addStar(player, reward);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        generateParticleEffect(player);
    }

    private static void generateParticleEffect(Player player) {
        double radius = 1.5;
        int circles = 6;
        int particlesPerCircle = 20;
        double heightIncrement = 0.5;
        World world = player.getWorld();
        Location center = player.getLocation().add(0.0, 0.5, 0.0);
        for (int circle = 0; circle < circles; ++circle) {
            for (int i = 0; i < particlesPerCircle; ++i) {
                double angle = Math.PI * 2 * (double) i / (double) particlesPerCircle;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                Location loc = center.clone().add(x, (double) circle * heightIncrement, z);
                world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}