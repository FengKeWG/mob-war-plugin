package org.windguest.mobwar.Mobs;

import java.util.Arrays;
import java.util.List;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.windguest.mobwar.Games.*;
import org.windguest.mobwar.Main;

public class Piglin implements Listener {

    private static final Main plugin = Main.getInstance();

    private static final ItemStack arrow = Items.createItem(
            Material.SPECTRAL_ARROW,
            "§f箭",
            Arrays.asList(
                    "§7猪灵的箭",
                    "",
                    "§c❤ §f伤害  6 - 11  点"
            ),
            null, 0, null, 1
    );

    private static void toAxe(Player player) {
        Players.setJobData(player, "piglin_axe");
        Disguise.disguise(player, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 15 * 20, 2, true, false, true));
        replaceItem(player, Material.GOLDEN_SWORD, Items.createItem(Material.GOLDEN_AXE, "§b猪灵的斧头",
                Arrays.asList(
                        "§7猪灵的斧头",
                        "",
                        "§c❤ §f伤害 10  §6❇§f 攻速 较慢"
                ),
                Enchantment.SHARPNESS, 7,
                null, 1));
        new BukkitRunnable() {
            @Override
            public void run() {
                toSword(player);
                if (Players.getJobData(player).equals("piglin_axe")) {
                    Players.setJobData(player, "piglin");
                    Disguise.undisguise(player);
                }
            }
        }.runTaskLater(plugin, 15 * 20L);
    }

    private static void toSword(Player player) {
        replaceItem(player, Material.GOLDEN_AXE, Items.createItem(Material.GOLDEN_SWORD, "§b猪灵的剑",
                Arrays.asList(
                        "§7猪灵的剑",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                Enchantment.SHARPNESS, 2,
                null, 1));
    }

    private static void replaceItem(Player player, Material original, ItemStack replacement) {
        ItemStack[] inventory = player.getInventory().getContents();
        for (int i = 0; i < inventory.length; ++i) {
            if (inventory[i] == null || inventory[i].getType() != original) continue;
            player.getInventory().setItem(i, replacement);
            break;
        }
    }

    private void summonHoglin(Player player) {
        Players.setJobData(player, "piglin_hoglin");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIGLIN_CELEBRATE, 1.0f, 1.0f);
        Disguise.disguise(player, 2);
        Location location = player.getLocation();
        Hoglin hoglin = (Hoglin) player.getWorld().spawnEntity(location, EntityType.HOGLIN);
        hoglin.setBaby();
        hoglin.setMaxHealth(20);
        hoglin.setRemoveWhenFarAway(false);
        hoglin.setSilent(true);
        hoglin.setCustomName(player.getName() + "的疣猪兽");
        hoglin.setCustomNameVisible(true);
        hoglin.setImmuneToZombification(true);
        hoglin.addPassenger(player);
        Entities.addEntity(hoglin, player, 0);
        BukkitTask task = new BukkitRunnable() {
            double fallSpeed = 0.0;
            int ticks = 0;

            public void run() {
                if (ticks >= 15 * 20) {
                    Entities.removeEntity(hoglin);
                    if (Players.getJobData(player).equals("piglin_hoglin")) {
                        Players.setJobData(player, "piglin");
                    }
                    Disguise.undisguise(player);
                    cancel();
                }
                ticks++;
                if (hoglin.isValid() && player.isInsideVehicle() && player.getVehicle() == hoglin) {
                    Location playerLoc = player.getLocation();
                    Vector direction = playerLoc.getDirection().normalize();
                    Location front = playerLoc.clone().add(direction.clone().multiply(1.5));
                    Location aboveFront = front.clone().add(0.0, 1.0, 0.0);
                    Location below = playerLoc.clone().subtract(0.0, 1.0, 0.0);
                    if (front.getBlock().getType().isSolid() && !aboveFront.getBlock().getType().isSolid()) {
                        direction.setY(0.5);
                    } else if (!below.getBlock().getType().isSolid()) {
                        fallSpeed += 0.1;
                        direction.setY(-fallSpeed);
                    } else {
                        fallSpeed = 0.0;
                        direction.setY(0);
                    }
                    hoglin.setVelocity(direction.multiply(0.4));
                    for (Entity entity : hoglin.getNearbyEntities(1.0, 1.0, 1.0)) {
                        if (!(entity instanceof LivingEntity livingEntity) || entity == player) continue;
                        if (livingEntity instanceof Player targetPlayer) {
                            if (Players.isTeam(player, targetPlayer)) continue;
                        }
                        livingEntity.damage(4, player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        Players.addTaskToPlayer(player, task);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Hoglin) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Hoglin && Entities.getEntityOwner(event.getEntity()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle() && player.getVehicle() instanceof Hoglin hoglin && Entities.getEntityOwner(hoglin) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            if (Players.getJobData(killer).equals("猪灵")) {
                if (event.getEntity() instanceof Player) {
                    killer.getInventory().addItem(Items.createGoldIngot(3));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                if (itemType == Material.GOLD_NUGGET && itemMeta.getDisplayName().equals("§c残暴蛮兵") && Players.getJobFromPlayer(player).equals("猪灵")) {
                    event.setCancelled(true);
                    toAxe(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIGLIN_CONVERTED_TO_ZOMBIFIED, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 0, true, false, true));
                    Energy.startDecay(player, "§c残暴蛮兵", 15);
                } else if (itemType == Material.GOLDEN_CARROT && itemMeta.getDisplayName().equals("§e友谊的力量") && Players.getJobFromPlayer(player).equals("猪灵")) {
                    event.setCancelled(true);
                    summonHoglin(player);
                    Energy.startDecay(player, "§e友谊的力量", 15);
                }
            }
        }
    }


    public static void start(Player player) {
        Players.setJobData(player, "piglin");
        Energy.createBar(player, Material.GOLD_NUGGET, "§c残暴蛮兵", BarColor.RED, 10, 10);
        Energy.createBar(player, Material.GOLDEN_CARROT, "§e友谊的力量", BarColor.YELLOW, 30, 30);
        Items.giveArrow(player, arrow, 3, 5);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.GOLDEN_HELMET,
                "§e猪灵头盔",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.PROTECTION,
                1,
                Color.fromRGB(255, 165, 0),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.GOLDEN_CHESTPLATE,
                "§e猪灵胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 5"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.GOLDEN_LEGGINGS,
                "§e猪灵护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.GOLDEN_BOOTS,
                "§e猪灵靴子",
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
                Material.GOLDEN_SWORD,
                "§b猪灵的剑",
                Arrays.asList(
                        "§7猪灵的剑",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                Enchantment.SHARPNESS,
                2,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.CROSSBOW,
                "§b猪灵的弩",
                Arrays.asList(
                        "§7猪灵的弩",
                        "§7长按§f[右键]§7蓄力",
                        "§7消耗箭矢进行射击",
                        "",
                        "§c❤ §f伤害 7 - 11"
                ),
                Enchantment.QUICK_CHARGE,
                2,
                null,
                1
        ));

        inv.setItem(2, arrow);

        inv.setItem(8, Items.createItem(
                Material.GOLDEN_APPLE,
                "§f金苹果",
                List.of(
                        "§7长按§f[右键]§7食用恢复生命"
                ),
                null,
                0,
                null,
                12
        ));

        inv.setItem(3, Items.createItem(
                Material.GOLD_NUGGET,
                "§c残暴蛮兵",
                Arrays.asList(
                        "§7将剑替换为斧头",
                        "§7并获得速度效果 I",
                        "",
                        "§a❖ §f能量 10  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(4, Items.createItem(
                Material.GOLDEN_CARROT,
                "§e友谊的力量",
                Arrays.asList(
                        "§7骑乘一只幼年疣猪兽",
                        "§7疣猪兽可以对生物造成伤害",
                        "",
                        "§c❤ §f伤害 4  §a❖ §f能量 30  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));
    }
}

