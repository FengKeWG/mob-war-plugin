package org.windguest.mobwar.Mobs;

import java.util.Arrays;
import java.util.List;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.windguest.mobwar.Games.*;
import org.windguest.mobwar.Main;

public class Wither implements Listener {

    private static final Main plugin = Main.getInstance();

    private void summonWitherSkeletons(Player player) {
        for (int i = 0; i < 5; ++i) {
            WitherSkeleton skeleton = (WitherSkeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON);
            skeleton.setCustomName(player.getName() + "的凋零骷髅");
            skeleton.setCustomNameVisible(true);
            skeleton.setPersistent(false);
            skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 0));
            Entities.addEntity(skeleton, player, 0);
            Bukkit.getScheduler().runTaskLater(plugin, () -> Entities.removeEntity(skeleton), 15 * 20L);
        }
    }

    private void witherFly(Player player) {
        Players.setJobData(player, "wither_fly");
        Players.addSkill(player, 1);
        Disguise.disguise(player, 0);
        Players.setPlayerFly(player, 0.025f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.getJobData(player).equals("wither_fly")) {
                Players.setJobData(player, "wither");
                Disguise.undisguise(player);
            }
            Players.removeSkill(player);
            Players.disablePlayerFly(player);
        }, 15 * 20L);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof WitherSkeleton skeleton) {
            if (Entities.getEntityOwner(skeleton) == null) {
                return;
            }
            if (event.getTarget() instanceof Player player) {
                Player target = Entities.getEntityOwner(skeleton);
                if (player.equals(target) || Players.isTeam(player, target)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WitherSkull skull) {
            if (Entities.getEntityOwner(skull) == null) return;
            Player owner = Entities.getEntityOwner(skull);
            if (skull.isCharged()) {
                Tools.bomb(skull.getLocation(), 3, owner, 12, 1.0, true, new PotionEffect(PotionEffectType.WITHER, 5 * 20, 2));
            } else {
                Tools.bomb(skull.getLocation(), 2, owner, 7, 0.5, false, new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1));
            }
            skull.getWorld().playSound(skull.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof WitherSkull skull) {
            if (Entities.getEntityOwner(skull) == null) return;
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof WitherSkeleton) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (Players.getJobFromPlayer(attacker).equals("凋灵")) {
                if (event.getEntity() instanceof LivingEntity victim) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 0));
                }
            }
        }
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            PotionEffect newEffect = event.getNewEffect();
            if (newEffect != null && newEffect.getType() == PotionEffectType.WITHER) {
                if (newEffect.getAmplifier() == 1) {
                    event.setCancelled(true);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 0));
                }
                if (Players.getJobData(player).contains("wither")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void bombWither(Player player) {
        Players.cancelPlayerAllTasks(player);
        Items.giveItemsInTimes(player, "§9凋灵的头颅", Material.WITHER_SKELETON_SKULL, 1, 3);
        Location explosionCenter = player.getLocation();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        Tools.bomb(explosionCenter, 7, player, 15, 1.0, true, new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Players.cancelPlayerAllTasks(player);
            Items.giveItemsInTimes(player, "§9凋灵的头颅", Material.WITHER_SKELETON_SKULL, 3, 3);
        }, 15 * 20L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();
            if (itemType == Material.WITHER_SKELETON_SKULL) {
                if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                    event.setCancelled(true);
                    int total = Items.countItems(player, Material.WITHER_SKELETON_SKULL);
                    WitherSkull skull = player.launchProjectile(WitherSkull.class);
                    skull.setIsIncendiary(false);
                    if (Players.getSkill(player) == 1) {
                        Vector direction = player.getEyeLocation().getDirection();
                        Vector velocity = direction.multiply(5.0);
                        skull.setVelocity(velocity);
                        skull.setCharged(true);
                    }
                    skull.setYield(0.0f);
                    Entities.addEntity(skull, player, 0);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f);
                    if (total == 1) {
                        Items.replaceItem(player, "§9凋灵的头颅", Material.BARRIER);
                    } else {
                        Items.reduceItemsInInventory(player, Material.WITHER_SKELETON_SKULL);
                    }
                }
            }
            if (itemType == Material.COAL && itemMeta.getDisplayName().equals("§9愤怒") && Players.getJobFromPlayer(player).equals("凋灵")) {
                event.setCancelled(true);
                bombWither(player);
                Energy.startDecay(player, "§9愤怒", 15);
            } else if (itemType == Material.NETHER_STAR && itemMeta.getDisplayName().equals("§c风暴") && Players.getJobFromPlayer(player).equals("凋灵")) {
                event.setCancelled(true);
                witherFly(player);
                summonWitherSkeletons(player);
                Energy.startDecay(player, "§c风暴", 15);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "wither");
        Items.giveItemsInTimes(player, "§9凋灵的头颅", Material.WITHER_SKELETON_SKULL, 3, 3);
        Energy.createBar(player, Material.COAL, "§9愤怒", BarColor.BLUE, 15, 15);
        Energy.createBar(player, Material.NETHER_STAR, "§c风暴", BarColor.RED, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§9凋灵头盔",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 1"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.BLACK,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§9凋灵胸甲",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 3"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.BLACK,
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.IRON_LEGGINGS,
                "§9凋灵护腿",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 5"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                null,
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§9凋灵靴子",
                Arrays.asList(
                        "§6火焰保护 I",
                        "",
                        "§b🛡 §f防御 2"
                ),
                Enchantment.FIRE_PROTECTION, 1,
                Color.BLACK,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7凋灵的剑",
                        "§7攻击使敌人凋零",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.WITHER_SKELETON_SKULL,
                "§9凋灵的头颅",
                Arrays.asList(
                        "§7按§f[右键]§7释放凋灵的头颅",
                        "§7产生爆炸伤害和凋零伤害",
                        "",
                        "§c❤ §f伤害 7  §b✳ §f冷却 3 秒  §e☒ §f上限 3 个"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.COAL,
                "§9愤怒",
                Arrays.asList(
                        "§7在你的位置产生一次大爆炸",
                        "§7头颅冷却变为 1 秒",
                        "",
                        "§c❤ §f伤害 15  §a❖ §f能量 15  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.NETHER_STAR,
                "§c风暴",
                Arrays.asList(
                        "§7期间发射蓝色头颅",
                        "§7提高凋灵头颅的伤害和爆炸范围",
                        "§7并且生成 5 只凋灵骷髅",
                        "§7同时可以进行飞行",
                        "",
                        "§c❤ §f伤害 12  §a❖ §f能量 20  §e ⏱ §f时长 15 秒"
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

