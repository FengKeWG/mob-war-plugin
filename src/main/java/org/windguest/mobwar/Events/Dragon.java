package org.windguest.mobwar.Events;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.Games.Players;

import java.util.*;

public class Dragon {

    private static final Random random = new Random();
    private static Player dragonPlayer = null;
    private static final Map<UUID, Integer> dragonPlayerTime = new HashMap<>();
    private static final int eventDuration = 300;
    static Main plugin = Main.getInstance();

    public static void startEvent() {
        Bukkit.broadcastMessage("§b[🎮] 末影龙事件开始！成为末影龙，获得巨额奖励！");
        EventsMain.playDragonRoar();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c末影龙事件开始！", "§a成为末影龙，获得巨额奖励！", 10, 70, 20);
        }
        EventsMain.setEvent("末影龙", eventDuration);
        setDragonPlayer(selectRandomValidPlayer());
        new BukkitRunnable() {
            public void run() {
                if (!EventsMain.isEventActive()) {
                    cancel();
                    return;
                }
                UUID dragonPlayerUUID = dragonPlayer.getUniqueId();
                int newTime = dragonPlayerTime.getOrDefault(dragonPlayerUUID, 0) + 1;
                dragonPlayerTime.put(dragonPlayerUUID, newTime);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public static void endEvent() {
        EventsMain.sendLeaderboard(dragonPlayerTime, "末影龙", "秒");
        dragonPlayer = null;
        dragonPlayerTime.clear();
    }

    public static void setDragonPlayer(Player player) {
        if (player != null) {
            dragonPlayer = player;
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
            player.setMaxHealth(40.0);
            player.setHealth(40.0);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("", "§e" + player.getName() + " 现在是 §5末影龙", 10, 70, 20);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        } else {
            new BukkitRunnable() {
                public void run() {
                    setDragonPlayer(selectRandomValidPlayer());
                }
            }.runTaskLater(plugin, 100L);
        }
    }

    public static Player selectRandomValidPlayer() {
        List<Player> validPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String job = Players.getJobFromPlayer(player);
            if (!job.equals("?")) {
                validPlayers.add(player);
            }
        }
        if (!validPlayers.isEmpty()) {
            return validPlayers.get(random.nextInt(validPlayers.size()));
        }
        return null;
    }

    public static Player getDragonPlayer() {
        return dragonPlayer;
    }

    public static int getDragonTime(Player player) {
        return dragonPlayerTime.getOrDefault(player.getUniqueId(), 0);
    }
}
