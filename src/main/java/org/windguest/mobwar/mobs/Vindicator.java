package org.windguest.mobwar.mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.windguest.mobwar.games.Disguise;
import org.windguest.mobwar.games.Energy;
import org.windguest.mobwar.games.Items;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

import java.util.Arrays;
import java.util.List;

public class Vindicator implements Listener {

    private static final Main plugin = Main.getInstance();

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!Players.getJobFromPlayer(player).equals("卫道士")) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_AXE) {
            if (player.hasCooldown(Material.IRON_AXE)) {
                event.setCancelled(true);
            } else if (Players.getSkill(player) == 1) {
                player.setCooldown(Material.IRON_AXE, 2 * 20);
            } else {
                player.setCooldown(Material.IRON_AXE, 5 * 20);
            }
        }
    }

    public void johnny(Player player) {
        player.setCooldown(Material.IRON_AXE, 0);
        Players.addSkill(player, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1.0f, 1.0f);
        Players.setJobData(player, "vindicator_johnny");
        Disguise.disguise(player, 0);
        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;

            public void run() {
                if (ticks >= 20 * 20) {
                    if (Players.getJobData(player).equals("vindicator_johnny")) {
                        Players.setJobData(player, "vindicator");
                        Disguise.undisguise(player);
                    }
                    Players.removeSkill(player);
                    cancel();
                    return;
                }
                ticks += 10;
                Location playerLocation = player.getLocation();
                int particleCount = 7 * 20;
                double angleIncrement = Math.PI * 2 / particleCount;
                for (double angle = 0.0; angle < Math.PI * 2; angle += angleIncrement) {
                    for (double height = 0.0; height <= 2.0; height += 1.0) {
                        double x = 7 * Math.cos(angle);
                        double z = 7 * Math.sin(angle);
                        Location particleLocation = playerLocation.clone().add(x, height, z);
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(128, 128, 128), 1.0f);
                        player.getWorld().spawnParticle(Particle.DUST, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
                    }
                }
                for (Entity entity : player.getNearbyEntities(7, 7, 7)) {
                    if (!(entity instanceof LivingEntity livingEntity)) continue;
                    if (entity instanceof Player targetPlayer && Players.isTeam(player, targetPlayer)) {
                        continue;
                    }
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 20, 0));
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
        Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                if (itemType == Material.COAL && itemMeta.getDisplayName().equals("§c狂暴") && Players.getJobFromPlayer(player).equals("卫道士")) {
                    event.setCancelled(true);
                    Disguise.disguise(player, 0);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_CELEBRATE, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 20, 0, true, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 0, true, false, true));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Disguise.undisguise(player), 20 * 20L);
                    Energy.startDecay(player, "§c狂暴", 20);
                } else if (itemType == Material.IRON_INGOT && itemMeta.getDisplayName().equals("§cJohnny") && Players.getJobFromPlayer(player).equals("卫道士")) {
                    event.setCancelled(true);
                    johnny(player);
                    Energy.startDecay(player, "§cJohnny", 20);
                }
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "vindicator");
        Energy.createBar(player, Material.COAL, "§c狂暴", BarColor.RED, 10, 10);
        Energy.createBar(player, Material.IRON_INGOT, "§cJohnny", BarColor.RED, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.IRON_HELMET,
                "§f卫道士帽子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§f卫道士胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 6"
                ),
                null,
                0,
                Color.fromRGB(20, 20, 20),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§f卫道士护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(60, 60, 60),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§f卫道士靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_AXE,
                "§f磨损的斧头",
                Arrays.asList(
                        "§7卫道士磨损的斧头",
                        "",
                        "§c❤ §f伤害 10  §6❇ §f攻速 慢"
                ),
                Enchantment.SHARPNESS,
                1,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.IRON_AXE,
                "§f锋利的斧头",
                Arrays.asList(
                        "§7卫道士锋利的斧头",
                        "",
                        "§c❤ §f伤害 15  §6❇ §f攻速 中等  §b✳ §f冷却 5 秒"
                ),
                Enchantment.SHARPNESS,
                11,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.COAL,
                "§c狂暴",
                Arrays.asList(
                        "§7获得速度 I 和力量 I",
                        "",
                        "§a❖ §f能量 10  §e ⏱ §f时长 20 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.IRON_INGOT,
                "§cJohnny",
                Arrays.asList(
                        "§7获得 Johnny 的力量",
                        "§7锋利的斧头冷却减少为 2 秒",
                        "§7同时使附近的玩家虚弱",
                        "",
                        "§a❖ §f能量 20  §e ⏱ §f时长 20 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(7, Items.createItem(
                Material.GOLDEN_APPLE,
                "§f金苹果",
                List.of(
                        "§7长按§f[右键]§7食用恢复生命"
                ),
                null,
                0,
                null,
                3
        ));

        inv.setItem(8, Items.createItem(
                Material.APPLE,
                "§f苹果",
                Arrays.asList(
                        "§7这是一些苹果",
                        "§7长按§f[右键]§7恢复饱食度"
                ),
                null,
                0,
                null,
                16
        ));
    }

}

