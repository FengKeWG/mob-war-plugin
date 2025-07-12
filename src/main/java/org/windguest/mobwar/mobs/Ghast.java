package org.windguest.mobwar.mobs;

import java.util.*;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

public class Ghast implements Listener {

    private static final Main plugin = Main.getInstance();

    private void ghastFly(Player player) {
        Players.setJobData(player, "ghast_fly");
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 15 * 20, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 0, true, false, true));
        Players.addSkill(player, 1);
        Disguise.disguise(player, 0);
        Players.setPlayerFly(player, 0.025f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.getJobData(player).equals("ghast_fly")) {
                Players.setJobData(player, "ghast");
            }
            Disguise.undisguise(player);
            Players.removeSkill(player);
            Players.disablePlayerFly(player);
        }, 15 * 20L);
    }

    private void bigFireBall(Player player) {
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setIsIncendiary(false);
        fireball.setYield(0.0f);
        Entities.addEntity(fireball, player, 1);
        new BukkitRunnable() {

            public void run() {
                if (fireball.isDead() || !fireball.isValid()) {
                    cancel();
                    return;
                }
                fireball.getWorld().spawnParticle(Particle.FLAME, fireball.getLocation(), 10, 0.2, 0.2, 0.2, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WitherSkull) return;
        if (entity instanceof Fireball fireball) {
            if (Entities.getEntityOwner(fireball) == null) return;
            Player shooter = Entities.getEntityOwner(fireball);
            int isBig = Entities.getEntityInfo(fireball);
            if (isBig == 1) {
                Players.setJobData(shooter, "ghast_big");
                Tools.bomb(fireball.getLocation(), 20, shooter, 20, 2.0, true, null);
                if (Players.getJobData(shooter).equals("ghast_big")) {
                    Players.setJobData(shooter, "ghast");
                }
            } else if (isBig == 0) {
                Tools.bomb(fireball.getLocation(), 5, shooter, 15, 1.0, true, null);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();

            if (itemMeta != null) {
                if (itemType == Material.FIRE_CHARGE && !player.hasCooldown(Material.FIRE_CHARGE)) {
                    if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                        event.setCancelled(true);
                        Fireball fireball = player.launchProjectile(Fireball.class);
                        fireball.setIsIncendiary(false);
                        fireball.setYield(0.0f);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
                        if (Players.getSkill(player) == 1) {
                            player.setCooldown(Material.FIRE_CHARGE, 20);
                            Disguise.disguiseAction(player);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_WARN, 1.0f, 1.0f);
                        } else {
                            player.setCooldown(Material.FIRE_CHARGE, 3 * 20);
                        }
                        Entities.addEntity(fireball, player, 0);
                    }
                }
                if (itemType == Material.GHAST_TEAR && itemMeta.getDisplayName().equals("§f恶魂之力") && Players.getJobFromPlayer(player).equals("恶魂")) {
                    event.setCancelled(true);
                    player.setCooldown(Material.FIRE_CHARGE, 0);
                    ghastFly(player);
                    Energy.startDecay(player, "§f恶魂之力", 15);

                } else if (itemType == Material.FIREWORK_STAR && itemMeta.getDisplayName().equals("§c超级火球") && Players.getJobFromPlayer(player).equals("恶魂")) {
                    event.setCancelled(true);
                    bigFireBall(player);
                    Energy.startDecay(player, "§c超级火球", 3);
                }
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "ghast");
        Energy.createBar(player, Material.GHAST_TEAR, "§f恶魂之力", BarColor.WHITE, 10, 10);
        Energy.createBar(player, Material.FIREWORK_STAR, "§c超级火球", BarColor.RED, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.IRON_HELMET,
                "§9恶魂头盔",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                null,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§9恶魂胸甲",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 3"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.fromRGB(230, 230, 230),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§9恶魂护腿",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.fromRGB(255, 255, 255),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§9恶魂靴子",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 1"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.fromRGB(230, 230, 230),
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7恶魂的剑",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.FIRE_CHARGE,
                "§f发射火球",
                Arrays.asList(
                        "§7按§f[右键]§7发射一枚火球",
                        "",
                        "§c❤§f伤害 15  §b✳ §f冷却 3 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.GHAST_TEAR,
                "§f恶魂之力",
                Arrays.asList(
                        "§7火球发射冷却减少为 1 秒",
                        "§7并且可以缓慢飞行",
                        "",
                        "§a❖ §f能量 10  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.FIREWORK_STAR,
                "§c超级火球",
                Arrays.asList(
                        "§7发射一枚伤害和爆炸范围巨大的火球",
                        "",
                        "§c❤§f伤害 20  §a❖ §f能量 20"
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

