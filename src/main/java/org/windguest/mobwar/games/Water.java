package org.windguest.mobwar.games;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.windguest.mobwar.Main;

public class Water implements Listener {

    private static final Main plugin = Main.getInstance();

    private static final Map<UUID, Integer> playerAir = new HashMap<>();

    public static void start(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerAir.put(playerUUID, 20);
        BukkitTask task = new BukkitRunnable() {

            public void run() {
                int air = playerAir.get(playerUUID);
                if (air <= 0) {
                    player.sendTitle("", "§c你脱水了！", 10, 70, 20);
                    player.damage(1.0);
                } else {
                    playerAir.put(playerUUID, air - 1);
                    player.setRemainingAir(0);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (isPlayerInWater(player)) {
            player.setRemainingAir(player.getMaximumAir());
            playerAir.put(player.getUniqueId(), 20);
        }
    }

    private boolean isPlayerInWater(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.WATER || block.getRelative(0, -1, 0).getType() == Material.WATER;
    }

    public static void removeAirPlayer(Player player) {
        playerAir.remove(player.getUniqueId());
    }
}