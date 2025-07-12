package org.windguest.mobwar.mobs;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

import java.util.Arrays;
import java.util.List;

public class Illusioners implements Listener {

    private static final Main plugin = Main.getInstance();

    public void debuff(Player player) { //debuff
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity instanceof Player p && Players.isTeam(player, p)) {
                continue;
            }
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 0, false, false, true));
        }
    }

    private void summonIllusioner(Player player) {
        Players.setJobData(player, "illusioner_summon");
        Disguise.disguise(player, 0);
        Location location = player.getLocation();
        for (int i = 0; i < 5; ++i) {
            Illusioner illusioner = location.getWorld().spawn(location, Illusioner.class);
            Entities.addEntity(illusioner, player, 0);
            illusioner.setCustomName(player.getName());
            illusioner.setCustomNameVisible(true);
            new BukkitRunnable() {
                public void run() {
                    Entities.removeEntity(illusioner);
                }
            }.runTaskLater(plugin, 15 * 20L);
        }
        new BukkitRunnable() {
            public void run() {
                if (Players.getJobData(player).equals("illusioner_summon")) {
                    Players.setJobData(player, "illusioner");
                }
                Disguise.undisguise(player);
            }
        }.runTaskLater(plugin, 15 * 20L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Illusioner illusioner) {
            if (Entities.getEntityOwner(illusioner) == null) {
                return;
            }
            Player owner = Entities.getEntityOwner(illusioner);
            ((Projectile) event.getDamager()).setShooter(owner);
            Entity entity = event.getEntity();
            if (entity instanceof Player victim) {
                if (Players.isTeam(owner, victim) || victim.equals(owner)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Illusioner illusioner) {
            if (Entities.getEntityOwner(illusioner) == null) {
                return;
            }
            Player owner = Entities.getEntityOwner(illusioner);
            Entity entity = event.getTarget();
            if (entity instanceof Player victim) {
                if (Players.isTeam(owner, victim) || victim.equals(owner)) {
                    event.setCancelled(true);
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
                if (itemType == Material.FLETCHING_TABLE && itemMeta.getDisplayName().equals("§9幻术·失明") && Players.getJobFromPlayer(player).equals("幻术师")) {
                    event.setCancelled(true);
                    debuff(player);
                    Energy.startDecay(player, "§9幻术·失明", 10);
                } else if (itemType == Material.GHAST_TEAR && itemMeta.getDisplayName().equals("§f幻术·分身") && Players.getJobFromPlayer(player).equals("幻术师")) {
                    event.setCancelled(true);
                    summonIllusioner(player);
                    Energy.startDecay(player, "§f幻术·分身", 15);
                }
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "illusionist");
        Energy.createBar(player, Material.FLETCHING_TABLE, "§9幻术·失明", BarColor.BLUE, 10, 10);
        Energy.createBar(player, Material.GHAST_TEAR, "§f幻术·分身", BarColor.WHITE, 20, 20);
    }

    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.DIAMOND_HELMET,
                "§f幻术师帽子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                null,
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§f幻术师胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                Color.fromRGB(0, 0, 128),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§f幻术师护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                Color.fromRGB(0, 0, 255),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.DIAMOND_BOOTS,
                "§f幻术师靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.BOW,
                "§1幻弓",
                Arrays.asList(
                        "§7幻术师的弓",
                        "§7长按§f[右键]§7蓄力",
                        "§7消耗箭矢进行射击",
                        "",
                        "§c❤ §f伤害 1 - 10"
                ),
                Enchantment.INFINITY,
                1,
                null,
                1
        ));

        inv.setItem(1, Items.createItem(
                Material.ARROW,
                "§f箭",
                List.of(
                        "§7幻术师的箭"
                ),
                null,
                0,
                null,
                64
        ));

        inv.setItem(2, Items.createItem(
                Material.FLETCHING_TABLE,
                "§9幻术·失明",
                Arrays.asList(
                        "§7令半径 5 格内的玩家失明",
                        "",
                        "§a❖ §f能量 10  §e ⏱ §f时长 10 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.GHAST_TEAR,
                "§f幻术·分身",
                Arrays.asList(
                        "§7自身变形为幻术师",
                        "§7并召唤 5 个复制体",
                        "",
                        "§a❖ §f能量 20  §e ⏱ §f时长 15 秒"
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