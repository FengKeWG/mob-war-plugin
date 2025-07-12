package org.windguest.mobwar.games;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.windguest.mobwar.Main;

import java.util.HashMap;
import java.util.Map;

public class Energy {

    private static final Main plugin = Main.getInstance();
    private static final Map<Player, Map<String, Data>> playerBossBars = new HashMap<>();

    public static void createBar(Player player, Material material, String name, BarColor color, double totalEnergy,
            double initialEnergy) {
        Map<String, Data> bossBars = playerBossBars.getOrDefault(player, new HashMap<>());
        Data existingBarData = bossBars.get(name);
        if (existingBarData != null) {
            existingBarData.getBossBar().removePlayer(player);
        }
        BossBar bossBar = player.getServer().createBossBar(name, color, BarStyle.SOLID);
        double initialProgress = initialEnergy / totalEnergy;
        bossBar.setProgress(initialProgress);
        Data bossBarData = new Data(bossBar, totalEnergy, initialEnergy, material);
        bossBar.addPlayer(player);
        bossBars.put(name, bossBarData);
        playerBossBars.put(player, bossBars);
    }

    private static void addEnergy(Player player, String name, double energy) {
        Map<String, Data> bossBars = playerBossBars.get(player);
        if (bossBars == null) {
            plugin.getLogger().warning("玩家 " + player.getName() + " 没有任何能量条。");
            return;
        }
        Data bossBarData = bossBars.get(name);
        if (bossBarData == null) {
            plugin.getLogger().warning("玩家 " + player.getName() + " 的ID为 " + name + " 的能量条不存在。");
            return;
        }
        double currentEnergy = bossBarData.getCurrentEnergy();
        double totalEnergy = bossBarData.getTotalEnergy();
        if (currentEnergy >= totalEnergy) {
            return;
        }
        double newEnergy = currentEnergy + energy;
        if (newEnergy >= totalEnergy) {
            newEnergy = totalEnergy;
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, SoundCategory.MASTER, 1.0f, 1.0f);
            player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 100, 0.5, 1, 0.5, 0.0,
                    null, true);
            Material material = bossBarData.getMaterial();
            Items.replaceItem(player, name, material);
        } else if (newEnergy < 0) {
            newEnergy = 0;
        }
        bossBarData.setCurrentEnergy(newEnergy);
        double progress = newEnergy / totalEnergy;
        bossBarData.getBossBar().setProgress(progress);
    }

    public static void addEnergyToAllBars(Player player, double energy) {
        Map<String, Data> bossBars = playerBossBars.get(player);
        if (bossBars != null) {
            for (Map.Entry<String, Data> entry : bossBars.entrySet()) {
                String name = entry.getKey();
                Data bossBarData = entry.getValue();
                if (isDecaying(bossBarData))
                    continue;
                addEnergy(player, name, energy);
            }
        }
    }

    private static boolean isDecaying(Data bossBarData) {
        BukkitTask t = bossBarData.getDecayTask();
        return t != null && !t.isCancelled();
    }

    public static void removeAllBars(Player player) {
        Map<String, Data> bossBars = playerBossBars.get(player);
        if (bossBars != null) {
            for (Data bossBarData : bossBars.values()) {
                bossBarData.getBossBar().removePlayer(player);
                if (bossBarData.getDecayTask() != null) {
                    bossBarData.getDecayTask().cancel();
                    bossBarData.setDecayTask(null);
                }
            }
            playerBossBars.remove(player);
        }
    }

    public static void removeBar(Player player, String name) {
        Map<String, Data> bossBars = playerBossBars.get(player);
        if (bossBars != null) {
            Data bossBarData = bossBars.get(name);
            if (bossBarData != null) {
                bossBarData.getBossBar().removePlayer(player);
                if (bossBarData.getDecayTask() != null) {
                    bossBarData.getDecayTask().cancel();
                    bossBarData.setDecayTask(null);
                }
                bossBars.remove(name);
                if (bossBars.isEmpty()) {
                    playerBossBars.remove(player);
                } else {
                    playerBossBars.put(player, bossBars);
                }
            }
        }
    }

    public static void startDecay(Player player, String name, int t) {
        Items.replaceItem(player, name, Material.BARRIER);
        Map<String, Data> bossBars = playerBossBars.get(player);
        if (bossBars == null) {
            plugin.getLogger().warning("未找到玩家 " + player.getName() + " 的任何能量条。");
            return;
        }
        Data bossBarData = bossBars.get(name);
        if (bossBarData == null) {
            plugin.getLogger().warning("未找到玩家 " + player.getName() + " 的ID为 " + name + " 的能量条。");
            return;
        }
        if (bossBarData.getCurrentEnergy() < bossBarData.getTotalEnergy()) {
            plugin.getLogger().warning("能量未满！");
            return;
        }
        if (isDecaying(bossBarData)) {
            plugin.getLogger().warning("正在衰减！");
            return;
        }

        // 计算总刻和每刻减少的能量
        int totalTicks = t * 20; // 20刻每秒
        if (totalTicks <= 0)
            totalTicks = 1; // 防止除以零
        double decrementPerTick = bossBarData.getTotalEnergy() / totalTicks;

        // 创建并调度衰减任务
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                double currentEnergy = bossBarData.getCurrentEnergy();
                double newEnergy = currentEnergy - decrementPerTick;

                if (newEnergy <= 0) {
                    // 设置能量为零并更新能量条
                    bossBarData.setCurrentEnergy(0);
                    bossBarData.getBossBar().setProgress(0.0);

                    // 执行指定命令
                    // executeCommands(player, id);

                    // 取消任务
                    this.cancel();

                    // 移除衰减任务引用
                    bossBarData.setDecayTask(null);
                } else {
                    // 更新当前能量和能量条进度
                    bossBarData.setCurrentEnergy(newEnergy);
                    double progress = newEnergy / bossBarData.getTotalEnergy();
                    bossBarData.getBossBar().setProgress(progress);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task, Players.TaskTag.DECAY);
        bossBarData.setDecayTask(task);
    }

    private static class Data {
        private final BossBar bossBar;
        private final double totalEnergy;
        private double currentEnergy;
        private BukkitTask decayTask;
        private final Material material;

        private Data(BossBar bossBar, double totalEnergy, double currentEnergy, Material material) {
            this.bossBar = bossBar;
            this.totalEnergy = totalEnergy;
            this.currentEnergy = currentEnergy;
            this.material = material;
        }

        private Material getMaterial() {
            return material;
        }

        private BossBar getBossBar() {
            return bossBar;
        }

        private double getTotalEnergy() {
            return totalEnergy;
        }

        private double getCurrentEnergy() {
            return currentEnergy;
        }

        private void setCurrentEnergy(double currentEnergy) {
            this.currentEnergy = currentEnergy;
        }

        private BukkitTask getDecayTask() {
            return decayTask;
        }

        private void setDecayTask(BukkitTask decayTask) {
            this.decayTask = decayTask;
        }
    }

}