package org.windguest.mobwar.Games;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.windguest.mobwar.Events.*;
import org.windguest.mobwar.Files;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.Mobs.*;

import java.util.*;

public class Players {

    private static final Map<UUID, List<BukkitTask>> playerTasks = new HashMap<>();
    private static final Map<UUID, Integer> playerStars = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerMaxStreaks = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerKills = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerDeaths = new HashMap<>();
    private static final Map<UUID, Player> playerDeathKiller = new HashMap<>();
    private static final Map<UUID, Integer> playerStreaks = new HashMap<>();
    private static final Map<UUID, String> playerData = new HashMap<>();
    private static final Map<UUID, String> playerJobs = new HashMap<>();
    private static final Map<UUID, Integer> playerSkillTemp = new HashMap<>();
    private static final Set<UUID> holePlayers = new HashSet<>();
    private static final Main plugin = Main.getInstance();

    public static void setJobData(Player player, String data) {
        UUID uuid = player.getUniqueId();
        playerData.put(uuid, data);
    }

    public static String getJobData(Player player) {
        UUID uuid = player.getUniqueId();
        return playerData.getOrDefault(uuid, "?");
    }

    public static void addSkill(Player player, int n) {
        UUID uuid = player.getUniqueId();
        playerSkillTemp.put(uuid, n);
    }

    public static void removeSkill(Player player) {
        UUID uuid = player.getUniqueId();
        playerSkillTemp.remove(uuid);
    }

    public static int getSkill(Player player) {
        UUID uuid = player.getUniqueId();
        return playerSkillTemp.getOrDefault(uuid, 0);
    }

    public static void addTaskToPlayer(Player player, BukkitTask task) {
        UUID uuid = player.getUniqueId();
        playerTasks.computeIfAbsent(uuid, k -> new ArrayList<>()).add(task);
    }

    public static String getPlayerPrefix(Player player) {
        return Jobs.getJobColorCode(player) + getTeamColor(player) + " " + Jobs.getJobIcon(player);
    }

    public static void addPlayerDeathKiller(Player victim, Player killer) {
        UUID uuid = victim.getUniqueId();
        playerDeathKiller.put(uuid, killer);
    }

    public static void removePlayerDeathKiller(Player victim) {
        UUID uuid = victim.getUniqueId();
        playerDeathKiller.remove(uuid);
    }

    public static Player getPlayerDeathKiller(Player victim) {
        UUID uuid = victim.getUniqueId();
        return playerDeathKiller.get(uuid);
    }

    public static void cancelPlayerAllTasks(Player player) {
        UUID uuid = player.getUniqueId();
        List<BukkitTask> tasks = playerTasks.remove(uuid);
        if (tasks != null) {
            tasks.forEach(BukkitTask::cancel);
        }
    }

    public static void clearPlayerAllData(Player player) {
        UUID uuid = player.getUniqueId();
        Energy.removeAllBars(player);
        Files.savePlayerData(player);
        Edit.removeEdit(player);
        playerStreaks.remove(uuid);
        playerData.remove(uuid);
        playerDeathKiller.remove(uuid);
        holePlayers.remove(uuid);
        player.setInvulnerable(false);
        removeSkill(player);
        removeJobFromPlayer(player);
        cancelPlayerAllTasks(player);
        clearPlayerProjectiles(player);
        player.setArrowsInBody(0);
        Bounty.clearData(player);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        disablePlayerFly(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 9, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 9, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        Disguise.clearDisguiseCount(player);
        Entities.removePlayer(player);
        Water.removeAirPlayer(player);
        Blaze.clearPlayerFires(player);
    }

    public static int getPlayerStreaks(Player player) {
        UUID uuid = player.getUniqueId();
        return playerStreaks.getOrDefault(uuid, 0);
    }

    public static void addJobToPlayer(Player player, String job) {
        UUID uuid = player.getUniqueId();
        playerJobs.put(uuid, job);
    }

    public static String getJobFromPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        return playerJobs.getOrDefault(uuid, "?");
    }

    public static void removeJobFromPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        playerJobs.remove(uuid);
    }

    public static int getPlayerStarsFromMap(UUID uuid) {
        return playerStars.getOrDefault(uuid, 0);
    }

    public static void setPlayerStarsInMap(UUID uuid, int amount) {
        playerStars.put(uuid, amount);
    }

    public static void setPlayerKillsInMap(UUID uuid, String job, int kills) {
        playerKills.computeIfAbsent(uuid, k -> new HashMap<>()).put(job, kills);
    }

    public static void setPlayerDeathsInMap(UUID uuid, String job, int deaths) {
        playerDeaths.computeIfAbsent(uuid, k -> new HashMap<>()).put(job, deaths);
    }

    public static void setPlayerMaxStreaksInMap(UUID uuid, String job, int maxStreaks) {
        playerMaxStreaks.computeIfAbsent(uuid, k -> new HashMap<>()).put(job, maxStreaks);
    }

    public static Map<String, Integer> getPlayerKillsMap(UUID uuid) {
        return playerKills.get(uuid);
    }

    public static Map<String, Integer> getPlayerDeathsMap(UUID uuid) {
        return playerDeaths.get(uuid);
    }

    public static Map<String, Integer> getPlayerMaxStreaksMap(UUID uuid) {
        return playerMaxStreaks.get(uuid);
    }

    public static void addStreak(Player player) {
        UUID uuid = player.getUniqueId();
        String job = getJobFromPlayer(player);
        playerStreaks.put(uuid, playerStreaks.getOrDefault(uuid, 0) + 1);
        Map<String, Integer> maxStreaks = playerMaxStreaks.getOrDefault(uuid, new HashMap<>());
        int currentStreak = playerStreaks.get(uuid);
        int maxStreakForJob = maxStreaks.getOrDefault(job, 0);
        if (currentStreak > maxStreakForJob) {
            maxStreaks.put(job, currentStreak);
            playerMaxStreaks.put(uuid, maxStreaks);
        }
    }

    public static void addKillInMap(Player player) {
        UUID uuid = player.getUniqueId();
        String job = getJobFromPlayer(player);
        addStreak(player);
        playerKills.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(job, 1, Integer::sum);
        playerKills.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge("总", 1, Integer::sum);
    }

    public static void addDeathInMap(Player player) {
        UUID uuid = player.getUniqueId();
        String job = getJobFromPlayer(player);
        playerDeaths.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(job, 1, Integer::sum);
        playerDeaths.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge("总", 1, Integer::sum);
    }

    public static int getPlayerKills(Player player, String job) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> kills = playerKills.get(uuid);
        if (kills == null) return 0;
        return kills.getOrDefault(job, 0);
    }

    public static int getPlayerDeaths(Player player, String job) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> deaths = playerDeaths.get(uuid);
        if (deaths == null) return 0;
        return deaths.getOrDefault(job, 0);
    }

    public static int getPlayerMaxStreaks(Player player, String job) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> maxStreaks = playerMaxStreaks.get(uuid);
        if (maxStreaks == null) return 0;
        return maxStreaks.getOrDefault(job, 0);
    }

    public static int getPlayerTotalMaxStreaks(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> maxStreaks = playerMaxStreaks.get(uuid);
        if (maxStreaks == null || maxStreaks.isEmpty()) {
            return 0;
        }
        return maxStreaks.values().stream().max(Integer::compareTo).orElse(0);
    }


    public static double getPlayerKDRatio(Player player, String job) {
        int kills = getPlayerKills(player, job);
        int deaths = getPlayerDeaths(player, job);
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    public static void removePlayerInHole(Player player) {
        UUID uuid = player.getUniqueId();
        holePlayers.remove(uuid);
    }

    public static void addPlayerInHole(Player player) {
        UUID uuid = player.getUniqueId();
        holePlayers.add(uuid);
    }

    public static boolean isPlayerInHole(Player player) {
        UUID uuid = player.getUniqueId();
        return holePlayers.contains(uuid);
    }

    public static String getTeamColor(Player player) {
        String team = Team.getTeam(player);
        if (team == null) {
            return "";
        } else {
            return team.equals("红队") ? "§c" : "§9";
        }
    }

    public static void addStar(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentStars = playerStars.getOrDefault(uuid, 0);
        playerStars.put(uuid, currentStars + amount);
    }

    public static int getStar(Player player) {
        return playerStars.getOrDefault(player.getUniqueId(), 0);
    }

    public static void syncStar(Player player) {
        UUID uuid = player.getUniqueId();
        int stars = playerStars.getOrDefault(uuid, 0);
        player.setLevel(stars);
        player.setExp(1.0f);
    }

    public static void giveHubItems(Player player) {
        ItemStack spawnCompass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = spawnCompass.getItemMeta();
        compassMeta.displayName(Component.text("§a选择生物§7（右键点击）"));
        compassMeta.lore(Collections.singletonList(Component.text("§7右键以打开模式菜单")));
        spawnCompass.setItemMeta(compassMeta);
        player.getInventory().setItem(0, spawnCompass);
        ItemStack editingBook = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = editingBook.getItemMeta();
        bookMeta.displayName(Component.text("§a编辑物品布局§7（右键点击）"));
        bookMeta.lore(Collections.singletonList(Component.text("§7右键以编辑物品布局")));
        editingBook.setItemMeta(bookMeta);
        player.getInventory().setItem(8, editingBook);
    }

    public static void clearPlayerProjectiles(Player player) {
        World world = player.getWorld();
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Projectile projectile && projectile.getShooter() == player) {
                entity.remove();
            }
        }
    }

    public static boolean isTeam(Player player1, Player player2) {
        String team1 = Team.getTeam(player1);
        String team2 = Team.getTeam(player2);
        return team1 != null && team1.equals(team2);
    }

    public static Player getHighestMaxHealthPlayer() {
        Player highestMaxHealthPlayer = null;
        double highestMaxHealth = 0.0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            double playerCurrentMaxHealth = player.getMaxHealth();
            if (playerCurrentMaxHealth > highestMaxHealth) {
                highestMaxHealth = playerCurrentMaxHealth;
                highestMaxHealthPlayer = player;
            }
        }
        return highestMaxHealthPlayer;
    }

    public static void setPlayerFly(Player player, float ascentSpeed) {
        player.setAllowFlight(true);
        player.setFlySpeed(ascentSpeed);
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            public void run() {
                if (ticks < 10) {
                    player.setFlying(true);
                    player.setVelocity(player.getVelocity().setY(ascentSpeed));
                    ++ticks;
                } else {
                    player.setVelocity(player.getVelocity().setY(0));
                    player.setFlying(true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        addTaskToPlayer(player, task);
    }

    public static void disablePlayerFly(Player player) {
        player.setFlySpeed(0.1f);
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    public static Player getTopPlayerFromMap(Map<UUID, Integer> map) {
        UUID topuuid = null;
        int maxValue = -1;
        for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
            UUID uuid = entry.getKey();
            int value = entry.getValue();
            if (value > maxValue) {
                maxValue = value;
                topuuid = uuid;
            }
        }
        if (topuuid != null) {
            return Bukkit.getPlayer(topuuid);
        }
        return null;
    }
}
