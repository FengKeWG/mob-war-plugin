package org.windguest.mobwar.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.Tools;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Sky {

    public static final Map<UUID, Integer> skyEventTime = new HashMap<>();
    private static final int eventDuration = 600;
    static Main plugin = Main.getInstance();

    public static void startEvent() {
        Bukkit.broadcastMessage("§b[🎮] 空岛事件开始！尽可能地占领瀑布空岛！");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c空岛事件开始！", "§a尽可能地占领瀑布空岛！", 10, 70, 20);
        }
        EventsMain.setEvent("空岛", eventDuration);
        World world = Bukkit.getWorld("world");
        for (Player player : Bukkit.getOnlinePlayers()) {
            String job = Players.getJobFromPlayer(player);
            if (!job.equals("?")) {
                teleportToSky(player);
            }
        }
        new BukkitRunnable() {

            public void run() {
                if (!EventsMain.isEventActive()) {
                    cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().getY() >= 155.0 && player.getLocation().getY() < 200.0) {
                        UUID playerUUID = player.getUniqueId();
                        int time = skyEventTime.getOrDefault(playerUUID, 0);
                        skyEventTime.put(playerUUID, time + 1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void endEvent() {
        EventsMain.sendLeaderboard(skyEventTime, "空岛", "秒");
        skyEventTime.clear();
    }

    public static Player getTopSkyPlayer() {
        return Players.getTopPlayerFromMap(skyEventTime);
    }

    public static int getTopSkyPlayerTime() {
        return Tools.getMaxValueFromMap(skyEventTime);
    }

    public static void teleportToSky(Player player) {
        World world = Bukkit.getWorld("world");
        Location center1 = new Location(player.getWorld(), -86, 0, 44);
        Location center2 = new Location(player.getWorld(), -82, 0, 70);
        Random random = new Random();
        Location center = random.nextBoolean() ? center1 : center2;
        double randomX = center.getX() + (random.nextDouble() * 10 - 5);
        double randomZ = center.getZ() + (random.nextDouble() * 10 - 5);
        double y = player.getWorld().getHighestBlockYAt((int) randomX, (int) randomZ);
        Location targetLocation = new Location(player.getWorld(), randomX, y + 1, randomZ);
        player.teleport(targetLocation);
    }
}
