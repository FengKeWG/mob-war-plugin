package org.windguest.mobwar.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.Games.Energy;
import org.windguest.mobwar.Games.Tools;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.Games.Players;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Crazy {

    private static final Map<UUID, Integer> crazyEventKills = new HashMap<>();
    private static final int eventDuration = 300;
    static Main plugin = Main.getInstance();

    public static void startEvent() {
        Bukkit.broadcastMessage("§b[🎮] 疯狂事件开始！所有玩家获得速度 II 和力量效果！");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c疯狂事件开始！", "§a所有玩家获得速度 II 和力量效果！", 10, 70, 20);
        }
        EventsMain.playDragonRoar();
        EventsMain.setEvent("疯狂", eventDuration);
        new BukkitRunnable() {
            public void run() {
                if (!EventsMain.isEventActive()) {
                    cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, true, false, true));
                    Energy.addEnergyToAllBars(player, 1);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public static void addKill(Player player) {
        UUID playerUUID = player.getUniqueId();
        int kills = Crazy.crazyEventKills.getOrDefault(playerUUID, 0);
        Crazy.crazyEventKills.put(playerUUID, kills + 1);
    }

    public static void endEvent() {
        EventsMain.sendLeaderboard(crazyEventKills, "疯狂", "🗡");
        crazyEventKills.clear();
    }

    public static Player getTopKiller() {
        return Players.getTopPlayerFromMap(crazyEventKills);
    }

    public static int getMaxKills() {
        return Tools.getMaxValueFromMap(crazyEventKills);
    }
}
