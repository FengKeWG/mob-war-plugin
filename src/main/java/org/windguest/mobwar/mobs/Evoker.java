package org.windguest.mobwar.mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Evoker implements Listener {

    private static final Main plugin = Main.getInstance();

    public static void summonVexes(Player player) {
        Players.setJobData(player, "evoker_vex");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);
        Disguise.disguise(player, 0);
        Location center = player.getLocation();
        for (int i = 0; i < 5; ++i) {
            Vex vex = center.getWorld().spawn(center, Vex.class);
            vex.setCustomName("§c" + player.getName() + " 的恼鬼");
            vex.setCustomNameVisible(true);
            Entities.addEntity(vex, player, 0);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Entities.removeEntity(vex);
                if (Players.getJobData(player).equals("evoker_vex")) {
                    Players.setJobData(player, "evoker");
                }
                Disguise.undisguise(player);
            }, 20 * 20L);
            BukkitTask task = new BukkitRunnable() {
                public void run() {
                    if (vex.isDead() || !vex.isValid()) {
                        Entities.removeEntity(vex);
                    } else {
                        Location playerLocation = player.getLocation();
                        if (vex.getLocation().distance(playerLocation) > 20.0) {
                            Location teleportLocation = playerLocation.clone().add(2.0 - Math.random() * 4.0, 0.0, 2.0 - Math.random() * 4.0);
                            vex.teleport(teleportLocation);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
            Players.addTaskToPlayer(player, task, Players.TaskTag.SKILL);
        }
    }

    public static void summonFangCircle(Player player) {
        Players.setJobData(player, "evoker_circle");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
        final Location center = player.getLocation();
        for (int i = 0; i < 300; ++i) {
            double angle = Math.random() * 2.0 * Math.PI;
            double r = (double) 10 * Math.sqrt(Math.random());
            double x = center.getX() + r * Math.cos(angle);
            double z = center.getZ() + r * Math.sin(angle);
            final Location fangLocation = new Location(center.getWorld(), x, center.getY(), z);
            EvokerFangs evokerFang = center.getWorld().spawn(fangLocation, EvokerFangs.class);
            evokerFang.setOwner(player);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Players.getJobData(player).equals("evoker_circle")) {
                Players.setJobData(player, "evoker");
            }
        }, 2 * 20L);
    }

    public static void generateLineFangs(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
        Location start = player.getLocation();
        double interval = (double) 20 / 20;
        double y = start.getY();
        for (int i = 0; i < 20; ++i) {
            double factor = i * interval;
            Location fangLocation = start.clone().add(player.getLocation().getDirection().multiply(factor));
            fangLocation.setY(y);
            new BukkitRunnable() {
                public void run() {
                    EvokerFangs evokerFang = (EvokerFangs) start.getWorld().spawnEntity(fangLocation, EntityType.EVOKER_FANGS);
                    evokerFang.setOwner(player);
                }
            }.runTask(plugin);
        }
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!Players.getJobFromPlayer(player).equals("唤魔者")) {
                return;
            }
            int totalTotems = Items.countItems(player, Material.TOTEM_OF_UNDYING);
            if (totalTotems == 1) {
                Items.replaceItem(player, "§9不死图腾", Material.BARRIER);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity entity = event.getEntity();
        if (entity instanceof Vex vex) {
            Player owner = Entities.getEntityOwner(vex);
            if (owner != null) {
                if (attacker.equals(owner)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (attacker instanceof Vex vex) {
            if (Entities.getEntityOwner(vex) == null) {
                return;
            }
            Player owner = Entities.getEntityOwner(vex);
            if (entity instanceof LivingEntity target) {
                if (entity instanceof Player victim) {
                    if (Players.isTeam(owner, victim) || victim.equals(owner)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                target.damage(10, owner);
                event.setCancelled(true);
            }
        }
        if (attacker instanceof EvokerFangs fang) {
            if (!(fang.getOwner() instanceof Player owner)) {
                return;
            }
            if (entity instanceof LivingEntity target) {
                if (entity instanceof Player victim) {
                    if (Players.isTeam(owner, victim) || victim.equals(owner)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                target.damage(8, owner);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Vex vex) {
            Player owner = Entities.getEntityOwner(vex);
            if (owner == null) {
                return;
            }
            if (event.getTarget() instanceof Player player) {
                if (owner.equals(player) || Players.isTeam(owner, player)) {
                    event.setCancelled(true);
                }
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

            if (itemType == Material.TOTEM_OF_UNDYING && !player.hasCooldown(Material.TOTEM_OF_UNDYING)
                    && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                    && Players.getJobFromPlayer(player).equals("唤魔者")) {
                generateLineFangs(player);
                player.setCooldown(Material.TOTEM_OF_UNDYING, 3 * 20);
                event.setCancelled(true);

            }
            if (itemType == Material.IRON_NUGGET && itemMeta.getDisplayName().equals("§9尖牙矩阵") && Players.getJobFromPlayer(player).equals("唤魔者")) {
                event.setCancelled(true);
                summonFangCircle(player);
                Energy.startDecay(player, "§9尖牙矩阵", 2);

            } else if (itemType == Material.IRON_INGOT && itemMeta.getDisplayName().equals("§8恼鬼随从") && Players.getJobFromPlayer(player).equals("唤魔者")) {
                event.setCancelled(true);
                summonVexes(player);
                Energy.startDecay(player, "§8恼鬼随从", 20);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "evoker");
        Energy.createBar(player, Material.IRON_NUGGET, "§9尖牙矩阵", BarColor.BLUE, 10, 10);
        Energy.createBar(player, Material.IRON_INGOT, "§8恼鬼随从", BarColor.RED, 20, 20);
        Items.giveItemsInTimes(player, "§9不死图腾", Material.TOTEM_OF_UNDYING, 60, 2);
    }


    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§9唤魔者头盔",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null,
                0,
                Color.fromRGB(72, 0, 72),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§9唤魔者胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                Color.fromRGB(72, 0, 72),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.IRON_LEGGINGS,
                "§9唤魔者护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 5"
                ),
                null,
                0,
                Color.fromRGB(72, 0, 72),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.LEATHER_BOOTS,
                "§9唤魔者靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(72, 0, 72),
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.STONE_SWORD,
                "§f剑",
                Arrays.asList(
                        "§7唤魔者的剑",
                        "",
                        "§c❤ §f伤害 5  §6❇§f 攻速 较快"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(40, Items.createItem(
                Material.TOTEM_OF_UNDYING,
                "§9不死图腾",
                Arrays.asList(
                        "§7按§f[右键]§7释放尖牙",
                        "§7如果手中没有不死图腾",
                        "§7将不能释放尖牙",
                        "§7不死图腾随时间补充",
                        "",
                        "§c❤ §f伤害 10  §6❇§f 冷却 3 秒",
                        "§b✳ §f冷却 60 秒  §e ☒ §f上限 2 个"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.IRON_NUGGET,
                "§9尖牙矩阵",
                Arrays.asList(
                        "§7释放大量尖牙",
                        "",
                        "§c❤ §f伤害 8  §a❖ §f能量 10"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.IRON_INGOT,
                "§8恼鬼随从",
                Arrays.asList(
                        "§7生成 5 只恼鬼",
                        "§7恼鬼会跟随你攻击其他玩家",
                        "",
                        "§c❤ §f伤害 10  §a❖ §f能量 20  §e ⏱ §f时长 20 秒"
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

