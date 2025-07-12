package org.windguest.mobwar.mobs;

import java.util.*;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.windguest.mobwar.games.*;
import org.windguest.mobwar.Main;

public class Sniffer implements Listener {

    private static final Main plugin = Main.getInstance();
    private static final Random random = new Random();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!Players.getJobData(player).contains("sniffer")) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        if (cursorItem.getType() != Material.AIR && clickedItem != null && clickedItem.getType() == Material.CAULDRON) {
            if (cursorItem.getType() == Material.TORCHFLOWER
                    || cursorItem.getType() == Material.BARRIER
                    || cursorItem.getType() == Material.BRUSH
                    || cursorItem.getType() == Material.PITCHER_PLANT
                    || cursorItem.getType() == Material.SKELETON_SKULL) {
                event.setCancelled(true);
            } else {
                event.setCursor(new ItemStack(Material.AIR));
                event.setCancelled(true);
            }
        }
    }

    private void burnEntitiesInRadius(Player player) {
        for (Entity entity : player.getNearbyEntities(7, 7, 7)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity instanceof Player targetPlayer && Players.isTeam(player, targetPlayer)) {
                continue;
            }
            livingEntity.setFireTicks(15 * 20);
        }
    }

    private void poisonEntitiesInRadius(Player player) {
        for (Entity entity : player.getNearbyEntities(7, 7, 7)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity instanceof Player targetPlayer && Players.isTeam(player, targetPlayer)) {
                continue;
            }
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 15 * 20, 2 - 1));
        }
    }

    private void randomReward(Location location, Player player) {
        String subtitle;
        int chance = random.nextInt(300);
        if (chance < 10) {
            int amount = random.nextInt(10) + 1;
            Emerald.dropEmerald(location, amount);
            subtitle = "§a你嗅探到了" + amount + "个绿宝石";
        } else if (chance < 20) {
            int amount = random.nextInt(12) + 5;
            ItemStack apples = new ItemStack(Material.APPLE, amount);
            addLoreAndHideFlags(apples);
            dropItem(location, apples);
            subtitle = "§a你嗅探到了" + amount + "个苹果";
        } else if (chance < 29) {
            int amount = random.nextInt(6) + 1;
            ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, amount);
            addLoreAndHideFlags(goldenApples);
            dropItem(location, goldenApples);
            subtitle = "§a你嗅探到了" + amount + "个金苹果";
        } else if (chance < 36) {
            int amount = random.nextInt(2) + 1;
            ItemStack enchantedGoldenApples = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, amount);
            addLoreAndHideFlags(enchantedGoldenApples);
            dropItem(location, enchantedGoldenApples);
            subtitle = "§a你嗅探到了" + amount + "个附魔金苹果";
        } else if (chance < 42) {
            int sharpnessLevel = random.nextInt(4) + 1;
            ItemStack sword = new ItemStack(Material.IRON_SWORD, 1);
            ItemMeta meta = sword.getItemMeta();
            meta.setDisplayName("§b锋利" + sharpnessLevel + "的铁剑");
            meta.addEnchant(Enchantment.SHARPNESS, sharpnessLevel, true);
            addLoreAndHideFlags(sword);
            sword.setItemMeta(meta);
            sword.setDurability((short) (sword.getType().getMaxDurability() - random.nextInt(20) - 1));
            dropItem(location, sword);
            subtitle = "§a你嗅探到了一把锋利" + sharpnessLevel + "的铁剑";
        } else if (chance < 48) {
            int sharpnessLevel = random.nextInt(4) + 1;
            ItemStack axe = new ItemStack(Material.IRON_AXE, 1);
            ItemMeta meta = axe.getItemMeta();
            meta.setDisplayName("§b锋利" + sharpnessLevel + "的铁斧");
            meta.addEnchant(Enchantment.SHARPNESS, sharpnessLevel, true);
            addLoreAndHideFlags(axe);
            axe.setItemMeta(meta);
            axe.setDurability((short) (axe.getType().getMaxDurability() - random.nextInt(20) - 1));
            dropItem(location, axe);
            subtitle = "§a你嗅探到了一把锋利" + sharpnessLevel + "的铁斧";
        } else if (chance < 57) {
            int amount = random.nextInt(8) + 1;
            ItemStack enderPearls = new ItemStack(Material.ENDER_PEARL, amount);
            addLoreAndHideFlags(enderPearls);
            dropItem(location, enderPearls);
            subtitle = "§a你嗅探到了" + amount + "个末影珍珠";
        } else if (chance < 70) {
            int amount = random.nextInt(28) + 3;
            dropItem(location, Items.createGoldIngot(amount));
            subtitle = "§a你嗅探到了" + amount + "个金锭";
        } else if (chance < 81) {
            ItemStack milkBucket = new ItemStack(Material.MILK_BUCKET, 1);
            addLoreAndHideFlags(milkBucket);
            dropItem(location, milkBucket);
            subtitle = "§a你嗅探到了奶桶";
        } else if (chance < 92) {
            ItemStack spyglass = new ItemStack(Material.SPYGLASS, 1);
            addLoreAndHideFlags(spyglass);
            dropItem(location, spyglass);
            subtitle = "§a你嗅探到了望远镜";
        } else if (chance < 103) {
            ItemStack turtleHelmet = new ItemStack(Material.TURTLE_HELMET, 1);
            addLoreAndHideFlags(turtleHelmet);
            dropItem(location, turtleHelmet);
            subtitle = "§a你嗅探到了海龟帽子";
        } else if (chance < 112) {
            ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            addLoreAndHideFlags(totem);
            dropItem(location, totem);
            subtitle = "§a你嗅探到了不死图腾";
        } else if (chance < 123) {
            ItemStack shield = new ItemStack(Material.SHIELD, 1);
            shield.setDurability((short) (shield.getType().getMaxDurability() - random.nextInt(20) - 1));
            addLoreAndHideFlags(shield);
            dropItem(location, shield);
            subtitle = "§a你嗅探到了盾牌";
        } else if (chance < 135) {
            int amount = random.nextInt(9) + 8;
            ItemStack eggs = new ItemStack(Material.EGG, amount);
            addLoreAndHideFlags(eggs);
            dropItem(location, eggs);
            subtitle = "§a你嗅探到了" + amount + "个鸡蛋";
        } else if (chance < 147) {
            int amount = random.nextInt(9) + 8;
            ItemStack snowballs = new ItemStack(Material.SNOWBALL, amount);
            addLoreAndHideFlags(snowballs);
            dropItem(location, snowballs);
            subtitle = "§a你嗅探到了" + amount + "个雪球";
        } else if (chance < 149) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack diamondHelmet = this.createEnchantedArmor(Material.DIAMOND_HELMET, "§b保护" + protectionLevel + "的钻石头盔", protectionLevel);
            this.setRandomDurability(diamondHelmet);
            dropItem(location, diamondHelmet);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的钻石头盔";
            addLoreAndHideFlags(diamondHelmet);
        } else if (chance < 150) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack diamondChestplate = this.createEnchantedArmor(Material.DIAMOND_CHESTPLATE, "§b保护" + protectionLevel + "的钻石胸甲", protectionLevel);
            this.setRandomDurability(diamondChestplate);
            dropItem(location, diamondChestplate);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的钻石胸甲";
            addLoreAndHideFlags(diamondChestplate);
        } else if (chance < 151) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack diamondLeggings = this.createEnchantedArmor(Material.DIAMOND_LEGGINGS, "§b保护" + protectionLevel + "的钻石护腿", protectionLevel);
            this.setRandomDurability(diamondLeggings);
            dropItem(location, diamondLeggings);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的钻石护腿";
            addLoreAndHideFlags(diamondLeggings);
        } else if (chance < 153) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack diamondBoots = this.createEnchantedArmor(Material.DIAMOND_BOOTS, "§b保护" + protectionLevel + "的钻石靴子", protectionLevel);
            this.setRandomDurability(diamondBoots);
            dropItem(location, diamondBoots);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的钻石靴子";
            addLoreAndHideFlags(diamondBoots);
        } else if (chance < 159) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack ironHelmet = this.createEnchantedArmor(Material.IRON_HELMET, "§b保护" + protectionLevel + "的铁头盔", protectionLevel);
            this.setRandomDurability(ironHelmet);
            dropItem(location, ironHelmet);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的铁头盔";
            addLoreAndHideFlags(ironHelmet);
        } else if (chance < 162) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack ironChestplate = this.createEnchantedArmor(Material.IRON_CHESTPLATE, "§b保护" + protectionLevel + "的铁胸甲", protectionLevel);
            this.setRandomDurability(ironChestplate);
            dropItem(location, ironChestplate);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的铁胸甲";
            addLoreAndHideFlags(ironChestplate);
        } else if (chance < 165) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack ironLeggings = this.createEnchantedArmor(Material.IRON_LEGGINGS, "§b保护" + protectionLevel + "的铁护腿", protectionLevel);
            this.setRandomDurability(ironLeggings);
            dropItem(location, ironLeggings);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的铁护腿";
            addLoreAndHideFlags(ironLeggings);
        } else if (chance < 171) {
            int protectionLevel = random.nextInt(4) + 1;
            ItemStack ironBoots = this.createEnchantedArmor(Material.IRON_BOOTS, "§b保护" + protectionLevel + "的铁靴子", protectionLevel);
            this.setRandomDurability(ironBoots);
            dropItem(location, ironBoots);
            subtitle = "§a你嗅探到了保护" + protectionLevel + "的铁靴子";
            addLoreAndHideFlags(ironBoots);
        } else if (chance < 180) {
            ItemStack suspiciousStew = this.createSuspiciousStew();
            dropItem(location, suspiciousStew);
            subtitle = "§a你嗅探到了迷之炖菜";
        } else if (chance < 189) {
            ItemStack splashPotion = this.createPotionItem(Material.SPLASH_POTION);
            dropItem(location, splashPotion);
            subtitle = "§a你嗅探到了喷溅药水";
        } else if (chance < 200) {
            ItemStack fireIronSword = new ItemStack(Material.IRON_SWORD, 1);
            ItemMeta meta = fireIronSword.getItemMeta();
            meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
            fireIronSword.setItemMeta(meta);
            fireIronSword.setDurability((short) (fireIronSword.getType().getMaxDurability() - random.nextInt(21) - 5));
            dropItem(location, fireIronSword);
            subtitle = "§a你嗅探到了火焰附加铁剑";
        } else if (chance < 210) {
            ItemStack knockbackIronSword = new ItemStack(Material.IRON_SWORD, 1);
            ItemMeta meta = knockbackIronSword.getItemMeta();
            int enchantmentLevel = random.nextInt(3) + 1;
            meta.addEnchant(Enchantment.KNOCKBACK, enchantmentLevel, true);
            meta.setDisplayName("§a击退铁剑");
            knockbackIronSword.setItemMeta(meta);
            knockbackIronSword.setDurability((short) (knockbackIronSword.getType().getMaxDurability() - random.nextInt(21) - 5));
            dropItem(location, knockbackIronSword);
            subtitle = "§a你嗅探到了击退铁剑";
            addLoreAndHideFlags(knockbackIronSword);
        } else if (chance < 220) {
            ItemStack punchBow = new ItemStack(Material.BOW, 1);
            ItemMeta meta = punchBow.getItemMeta();
            int enchantmentLevel = random.nextInt(3) + 1;
            meta.addEnchant(Enchantment.PUNCH, enchantmentLevel, true);
            meta.setDisplayName("§a冲击弓");
            punchBow.setItemMeta(meta);
            punchBow.setDurability((short) (punchBow.getType().getMaxDurability() - random.nextInt(21) - 5));
            dropItem(location, punchBow);
            subtitle = "§a你嗅探到了冲击弓";
            addLoreAndHideFlags(punchBow);
        } else if (chance < 230) {
            ItemStack flameBow = new ItemStack(Material.BOW, 1);
            ItemMeta meta = flameBow.getItemMeta();
            int enchantmentLevel = random.nextInt(3) + 1;
            meta.addEnchant(Enchantment.FLAME, enchantmentLevel, true);
            meta.setDisplayName("§a火矢弓");
            flameBow.setItemMeta(meta);
            flameBow.setDurability((short) (flameBow.getType().getMaxDurability() - random.nextInt(21) - 5));
            dropItem(location, flameBow);
            subtitle = "§a你嗅探到了火矢弓";
            addLoreAndHideFlags(flameBow);
        } else if (chance < 240) {
            ItemStack multishotCrossbow = new ItemStack(Material.CROSSBOW, 1);
            ItemMeta meta = multishotCrossbow.getItemMeta();
            meta.addEnchant(Enchantment.MULTISHOT, 1, true);
            multishotCrossbow.setItemMeta(meta);
            multishotCrossbow.setDurability((short) (multishotCrossbow.getType().getMaxDurability() - random.nextInt(21) - 5));
            addLoreAndHideFlags(multishotCrossbow);
            dropItem(location, multishotCrossbow);
            subtitle = "§a你嗅探到了多重射击弩";
        } else if (chance < 250) {
            ItemStack riptideTrident = new ItemStack(Material.TRIDENT, 1);
            ItemMeta meta = riptideTrident.getItemMeta();
            meta.addEnchant(Enchantment.RIPTIDE, 1, true);
            riptideTrident.setItemMeta(meta);
            riptideTrident.setDurability((short) (riptideTrident.getType().getMaxDurability() - random.nextInt(21) - 5));
            dropItem(location, riptideTrident);
            subtitle = "§a你嗅探到了激流三叉戟";
        } else if (chance < 258) {
            int amount = random.nextInt(16) + 5;
            ItemStack arrows = new ItemStack(Material.ARROW, amount);
            addLoreAndHideFlags(arrows);
            dropItem(location, arrows);
            subtitle = "§a你嗅探到了" + amount + "个弓箭";
        } else if (chance < 267) {
            ItemStack regularPotion = this.createPotionItem(Material.POTION);
            dropItem(location, regularPotion);
            subtitle = "§a你嗅探到了药水";
        } else if (chance < 276) {
            ItemStack lingeringPotion = this.createPotionItem(Material.LINGERING_POTION);
            dropItem(location, lingeringPotion);
            subtitle = "§a你嗅探到了滞留药水";
        } else if (chance < 285) {
            ItemStack tippedArrow = this.createTippedArrow();
            dropItem(location, tippedArrow);
            subtitle = "§a你嗅探到了药水箭";
        } else if (chance < 290) {
            int amount = random.nextInt(16) + 5;
            ItemStack arrows = new ItemStack(Material.SPECTRAL_ARROW, amount);
            addLoreAndHideFlags(arrows);
            dropItem(location, arrows);
            subtitle = "§a你嗅探到了" + amount + "个光灵箭";
        } else if (chance < 293) {
            int sharpnessLevel = random.nextInt(4);
            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
            ItemMeta meta = sword.getItemMeta();
            meta.setDisplayName("§b锋利" + sharpnessLevel + "的钻石剑");
            meta.addEnchant(Enchantment.SHARPNESS, sharpnessLevel, true);
            addLoreAndHideFlags(sword);
            sword.setItemMeta(meta);
            sword.setDurability((short) (sword.getType().getMaxDurability() - random.nextInt(20) - 1));
            dropItem(location, sword);
            subtitle = "§a你嗅探到了一把锋利" + sharpnessLevel + "的钻石剑";
        } else if (chance < 296) {
            int sharpnessLevel = random.nextInt(4);
            ItemStack axe = new ItemStack(Material.DIAMOND_AXE, 1);
            ItemMeta meta = axe.getItemMeta();
            meta.setDisplayName("§b锋利" + sharpnessLevel + "的钻石斧");
            meta.addEnchant(Enchantment.SHARPNESS, sharpnessLevel, true);
            addLoreAndHideFlags(axe);
            axe.setItemMeta(meta);
            axe.setDurability((short) (axe.getType().getMaxDurability() - random.nextInt(20) - 1));
            dropItem(location, axe);
            subtitle = "§a你嗅探到了一把锋利" + sharpnessLevel + "的钻石斧";
        } else {
            subtitle = "§7你嗅探到了空气";
        }
        player.sendTitle("", subtitle, 10, 70, 20);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNIFFER_DROP_SEED, 1.0f, 1.0f);
    }

    private ItemStack createSuspiciousStew() {
        ItemStack suspiciousStew = new ItemStack(Material.SUSPICIOUS_STEW, 1);
        SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) suspiciousStew.getItemMeta();
        PotionEffectType[] effects = new PotionEffectType[]{PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE, PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.INSTANT_HEALTH, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.JUMP_BOOST, PotionEffectType.NAUSEA, PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.BLINDNESS, PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION, PotionEffectType.GLOWING, PotionEffectType.LEVITATION, PotionEffectType.LUCK, PotionEffectType.UNLUCK, PotionEffectType.SLOW_FALLING, PotionEffectType.CONDUIT_POWER, PotionEffectType.DOLPHINS_GRACE, PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.DARKNESS};
        PotionEffectType effect = effects[random.nextInt(effects.length)];
        int duration = (random.nextInt(26) + 5) * 20;
        stewMeta.addCustomEffect(new PotionEffect(effect, duration, random.nextInt(2)), true);
        suspiciousStew.setItemMeta(stewMeta);
        addLoreAndHideFlags(suspiciousStew);
        return suspiciousStew;
    }

    private ItemStack createTippedArrow() {
        ItemStack tippedArrow = new ItemStack(Material.TIPPED_ARROW, random.nextInt(16) + 1);
        PotionMeta potionMeta = (PotionMeta) tippedArrow.getItemMeta();
        PotionEffectType[] effects = new PotionEffectType[]{PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE, PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.INSTANT_HEALTH, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.JUMP_BOOST, PotionEffectType.NAUSEA, PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.BLINDNESS, PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION, PotionEffectType.GLOWING, PotionEffectType.LEVITATION, PotionEffectType.LUCK, PotionEffectType.UNLUCK, PotionEffectType.SLOW_FALLING, PotionEffectType.CONDUIT_POWER, PotionEffectType.DOLPHINS_GRACE, PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.DARKNESS};
        PotionEffectType effect = effects[random.nextInt(effects.length)];
        int duration = (random.nextInt(26) + 5) * 20;
        potionMeta.addCustomEffect(new PotionEffect(effect, duration, random.nextInt(2)), true);
        potionMeta.setDisplayName(effect.getName().toUpperCase() + " 药水箭");
        tippedArrow.setItemMeta(potionMeta);
        addLoreAndHideFlags(tippedArrow);
        return tippedArrow;
    }

    private ItemStack createPotionItem(Material material) {
        ItemStack item = new ItemStack(material, 1);
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        PotionEffectType[] effects = new PotionEffectType[]{PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE, PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.INSTANT_HEALTH, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.JUMP_BOOST, PotionEffectType.NAUSEA, PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.BLINDNESS, PotionEffectType.NIGHT_VISION, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION, PotionEffectType.GLOWING, PotionEffectType.LEVITATION, PotionEffectType.LUCK, PotionEffectType.UNLUCK, PotionEffectType.SLOW_FALLING, PotionEffectType.CONDUIT_POWER, PotionEffectType.DOLPHINS_GRACE, PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.DARKNESS};
        PotionEffectType effect = effects[random.nextInt(effects.length)];
        int duration = (random.nextInt(26) + 5) * 20;
        potionMeta.addCustomEffect(new PotionEffect(effect, duration, random.nextInt(2)), true);
        potionMeta.setDisplayName(effect.getName().toUpperCase() + " 药水");
        item.setItemMeta(potionMeta);
        addLoreAndHideFlags(item);
        return item;
    }

    private void setRandomDurability(ItemStack item) {
        short maxDurability = item.getType().getMaxDurability();
        int durability = maxDurability - random.nextInt(maxDurability / 3) - maxDurability / 4;
        item.setDurability((short) durability);
    }

    private ItemStack createEnchantedArmor(Material material, String name, int protectionLevel) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.addEnchant(Enchantment.PROTECTION, protectionLevel, true);
        addLoreAndHideFlags(item);
        item.setItemMeta(meta);
        return item;
    }

    private void addLoreAndHideFlags(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setLore(List.of("§7远古文明留下的古老痕迹"));
        item.setItemMeta(meta);
    }

    private void dropItem(Location location, ItemStack item) {
        location.getWorld().dropItem(location, item).setVelocity(new Vector(0.0, 0.2, 0.0));
    }

    public static void superBrush(Player player) {
        player.setCooldown(Material.BRUSH, 0);
        Players.addSkill(player, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 0, true, false, true));
        Disguise.disguise(player, 0);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Players.removeSkill(player);
            Disguise.undisguise(player);
        }, 20 * 20L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (item != null) {
            Material itemType = item.getType();
            ItemMeta itemMeta = item.getItemMeta();

            if (itemType == Material.BRUSH && !player.hasCooldown(Material.BRUSH)
                    && !Edit.isEditing(player)
                    && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)) {
                event.setCancelled(true);
                if (Players.getSkill(player) == 1) {
                    player.setCooldown(Material.BRUSH, 20);
                } else {
                    player.setCooldown(Material.BRUSH, 10 * 20);
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNIFFER_DIGGING, 1.0f, 1.0f);
                    Location location = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
                    randomReward(location, player);
                }, 10L);

            }
            if (itemType == Material.TORCHFLOWER && itemMeta.getDisplayName().equals("§c火把花") && Players.getJobFromPlayer(player).equals("嗅探兽")) {
                event.setCancelled(true);
                burnEntitiesInRadius(player);
                Energy.startDecay(player, "§c火把花", 15);

            } else if (itemType == Material.PITCHER_PLANT && itemMeta.getDisplayName().equals("§2瓶子草") && Players.getJobFromPlayer(player).equals("嗅探兽")) {
                event.setCancelled(true);
                poisonEntitiesInRadius(player);
                Energy.startDecay(player, "§2瓶子草", 15);

            } else if (itemType == Material.SKELETON_SKULL && itemMeta.getDisplayName().equals("§f探古寻源") && Players.getJobFromPlayer(player).equals("嗅探兽")) {
                event.setCancelled(true);
                superBrush(player);
                Energy.startDecay(player, "§f探古寻源", 20);
            }
        }
    }

    public static void start(Player player) {
        Players.setJobData(player, "sniffer");
        Energy.createBar(player, Material.TORCHFLOWER, "§c火把花", BarColor.RED, 15, 15);
        Energy.createBar(player, Material.PITCHER_PLANT, "§2瓶子草", BarColor.GREEN, 15, 15);
        Energy.createBar(player, Material.SKELETON_SKULL, "§f探古寻源", BarColor.WHITE, 30, 30);
    }


    public static void equipKits(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        player.getInventory().setHelmet(Items.createItem(
                Material.LEATHER_HELMET,
                "§2嗅探兽头盔",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 1"
                ),
                null,
                0,
                Color.fromRGB(255, 215, 0),
                1
        ));

        player.getInventory().setChestplate(Items.createItem(
                Material.LEATHER_CHESTPLATE,
                "§2嗅探兽胸甲",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                Color.fromRGB(34, 139, 34),
                1
        ));

        player.getInventory().setLeggings(Items.createItem(
                Material.LEATHER_LEGGINGS,
                "§2嗅探兽护腿",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 3"
                ),
                null,
                0,
                Color.fromRGB(34, 139, 34),
                1
        ));

        player.getInventory().setBoots(Items.createItem(
                Material.IRON_BOOTS,
                "§2嗅探兽靴子",
                Arrays.asList(
                        "",
                        "§b🛡 §f防御 2"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(0, Items.createItem(
                Material.BRUSH,
                "§f刷子",
                Arrays.asList(
                        "§7对准方块§f[右键]§7进行嗅探",
                        "§7可以获得随机奖励",
                        "",
                        "§b✳ §f冷却 10 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(2, Items.createItem(
                Material.CAULDRON,
                "§f垃圾桶",
                List.of(
                        "§7将物品拖动到此处可以清除"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(3, Items.createItem(
                Material.TORCHFLOWER,
                "§c火把花",
                Arrays.asList(
                        "§7使附近的玩家着火",
                        "§7同时自身获得抗火效果",
                        "",
                        "§a❖ §f能量 15  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(4, Items.createItem(
                Material.PITCHER_PLANT,
                "§2瓶子草",
                Arrays.asList(
                        "§7使得附近的玩家中毒",
                        "§7同时自身获得抗性提升",
                        "",
                        "§a❖ §f能量 15  §e ⏱ §f时长 15 秒"
                ),
                null,
                0,
                null,
                1
        ));

        inv.setItem(5, Items.createItem(
                Material.SKELETON_SKULL,
                "§f探古寻源",
                Arrays.asList(
                        "§7刷子冷却缩短为 1 秒",
                        "§7同时获得缓慢和抗性提升",
                        "",
                        "§a❖ §f能量 30  §e ⏱ §f时长 20 秒"
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