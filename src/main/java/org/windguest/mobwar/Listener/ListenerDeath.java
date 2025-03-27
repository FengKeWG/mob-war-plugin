package org.windguest.mobwar.Listener;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.windguest.mobwar.Events.*;
import org.windguest.mobwar.Games.*;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.Games.Players;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListenerDeath implements Listener {

    static Main plugin = Main.getInstance();
    private static final Map<String, String> DEATH_MESSAGES = Map.ofEntries(
            Map.entry("blaze", "{killer} §7让 {victim} §7在火焰中消失！"),
            Map.entry("blaze_gun", "{killer} §7用火焰机枪将 {victim} §7烧成了灰烬！"),
            Map.entry("blaze_fly", "{killer} §7在火焰的包围中飞翔，将 {victim} §7瞬间融化！"),
            Map.entry("creeper", "{killer} §7炸飞了 {victim} §7！"),
            Map.entry("creeper_rain", "{killer} §7的天女散花将 {victim} §7炸成了碎片！"),
            Map.entry("creeper_bomb", "{killer} §7选择自爆，{victim} §7瞬间消失在爆炸中！"),
            Map.entry("evoker", "{killer} §7召唤尖牙矩阵将 {victim} §7吞噬殆尽！"),
            Map.entry("evoker_vex", "{killer} §7和他的恼鬼共同消灭了 {victim} §7！"),
            Map.entry("evoker_circle", "{killer} §7的尖牙撕裂了 {victim} §7！"),
            Map.entry("enderman", "{killer} §7瞬移到 {victim} §7身后，将其消灭！"),
            Map.entry("enderman_super", "{killer} §7近乎疯狂， {victim} §7消失在末影之中！"),
            Map.entry("ghast", "{killer} §7的火球爆炸终结了 {victim} §7的生命！"),
            Map.entry("ghast_fly", "{killer} §7显露本体，飞行的威压将 {victim} §7消灭！"),
            Map.entry("ghast_big", "{killer} §7用超级火球让 {victim} §7在剧烈的爆炸中灰飞烟灭！"),
            Map.entry("goat_rush", "{killer} §7将 {victim} §7创飞了！"),
            Map.entry("goat_strengthen", "{killer} §7吹响号角，力量暴增，将 {victim} §7击倒！"),
            Map.entry("goat", "{killer} §7一击之下，{victim} §7倒地不起！"),
            Map.entry("illusioner", "{killer} §7射出一箭，{victim} §7应声倒地！"),
            Map.entry("illusioner_summon", "{killer} §7召唤复制体围攻，{victim} §7在混乱中倒下！"),
            Map.entry("piglin", "{killer} §7挥动金剑，将 {victim} §7斩杀！"),
            Map.entry("piglin_axe", "{killer} §7化身猪灵蛮兵，挥动金斧头将 {victim} §7斩杀！"),
            Map.entry("piglin_hoglin", "{killer} §7和幼年疣猪兽的友谊无坚不摧，{victim} §7在他们的合力攻击下倒地！"),
            Map.entry("squid", "{killer} §7的墨水喷涌而出，{victim} §7在黑暗中被终结！"),
            Map.entry("squid_glow", "{killer} §7喷出荧光，{victim} §7在光影中被消灭！"),
            Map.entry("trader", "{killer} §7挥动手中的货物，将 {victim} §7击倒在地！"),
            Map.entry("trader_freeze", "{killer} §7使用羊驼战术，{victim} §7被定身在原地，被无情击败！"),
            Map.entry("vindicator", "{killer} §7挥舞斧头，将 {victim} §7击倒在地！"),
            Map.entry("vindicator_johnny", "{killer} §7获得了Johnny的力量，以猛烈一击终结了 {victim} §7的生命！"),
            Map.entry("warden", "{killer} §7的声波震撼了 {victim} §7，终结了他的生命！"),
            Map.entry("warden_super", "{killer} §7的声波震撼了 {victim} §7，终结了他的生命！"),
            Map.entry("warden_big", "{killer} §7释放大量声波，{victim} §7在猛烈震荡中被击碎！"),
            Map.entry("wither", "{killer} §7的头颅使 {victim} §7凋谢！"),
            Map.entry("wither_fly", "{killer} §7在空中俯瞰，强大的力量 使{victim} §7凋零！"),
            Map.entry("breeze", "{killer} §7的风弹席卷而来，{victim} §7被无情击倒！"),
            Map.entry("breeze_storm", "{killer} §7释放出强风 {victim} §7被吹飞并在冲击中消失！"),
            Map.entry("breeze_big", "{killer} §7释放出毁灭性的蓄能风弹，{victim} §7在狂暴的风中被撕裂成碎片！"),
            Map.entry("skeleton", "{killer} §7的箭矢精准命中，{victim} §7应声倒地！"),
            Map.entry("skeleton_slow", "{killer} §7发射缓慢箭矢，{victim} §7在中箭后倒地不起！"),
            Map.entry("skeleton_poison", "{killer} §7发射剧毒箭矢，{victim} §7在毒素蔓延中痛苦倒下！"),
            Map.entry("guardian_shoot", "{killer} §7的激光精准命中，{victim} §7瞬间毙命！"),
            Map.entry("guardian_max", "{killer} §7释放致命的瞬间激光，{victim} §7在光束中无力反抗，瞬间丧命！"),
            Map.entry("guardian_elder", "{killer} §7进化为远古守卫者，{victim} §7在挖掘疲劳中被击败！"),
            Map.entry("guardian", "{killer} §7挥动尖刺，{victim} §7应声倒地！"),
            Map.entry("sniffer_burn", "{killer} §7用火把花释放熊熊火焰，{victim} §7在烈焰中被击杀！"),
            Map.entry("sniffer_poison", "{killer} §7的瓶子草释放了致命毒素，{victim} §7在中毒中无力反抗！"),
            Map.entry("sniffer", "{killer} §7嗅出了敌人的方向，将{victim} §7迅速击败！"),
            Map.entry("sniffer_super", "{killer} §7嗅出了敌人的方向，将{victim} §7迅速击败！"),
            Map.entry("drowned", "{killer} §7的三叉戟破空而至，{victim} §7的胸膛被溺亡诅咒贯穿！"),
            Map.entry("drowned_light", "{killer} §7的引雷三叉戟直指苍穹，{victim} §7在雷霆怒啸中化作焦骸！"),
            Map.entry("default", "{killer} §7击杀了 {victim} ")
    );

    private static String getDeathString(String killerData) {
        return DEATH_MESSAGES.getOrDefault(killerData, DEATH_MESSAGES.get("default"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        World world = victim.getWorld();
        Player killer = Players.getPlayerDeathKiller(victim);
        event.setDeathMessage(null);
        Players.addDeathInMap(victim);
        playDeathSound(victim);
        String victimName = Players.getPlayerPrefix(victim) + " " + victim.getName();
        if (killer != null) {
            Players.addKillInMap(killer);
            int amount = Math.min(5, Players.getPlayerStreaks(killer));
            Emerald.giveEmerald(killer, amount);
            String killerJobData = Players.getJobData(killer);
            String killerPrefix = Players.getPlayerPrefix(killer);
            String killerName = killerPrefix + " " + killer.getName();
            if (Bounty.isPlayerBountied(victim)) {
                int bounty = Bounty.getPlayerBounties(victim);
                Emerald.giveEmerald(killer, bounty);
                Bounty.clearData(victim);
                Bukkit.broadcastMessage("§6[💰] 被悬赏的 §c" + victimName + " §6已被 §a" + killerName + " §6击败！§e" + bounty + "§6个§a§l绿宝石§6的酬劳已被Ta收入囊中！");
            }
            int starReward = new Random().nextInt(3) + 1;
            Players.addStar(killer, starReward);
            String starMessage = " §f[+" + starReward + "§b✦§f]";
            String messageTemplate = getDeathString(killerJobData);
            String formattedMessage = messageTemplate.replace("{killer}", killerName).replace("{victim}", victimName);
            formattedMessage += starMessage;
            Bukkit.broadcastMessage("[☠] " + formattedMessage);
            String subtitle = "§c§l🗡 §r" + victimName + starMessage;
            killer.sendTitle("", subtitle, 10, 70, 20);
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            playKillSound(killer, victim);
            Players.removePlayerDeathKiller(victim);
        } else {
            Bukkit.broadcastMessage("[☠] " + victimName + " §7死亡了");
        }
        spawnFirework(victim);
        event.setCancelled(true);
        Players.clearPlayerAllData(victim);
        victim.teleport(new Location(world, -14.5, 243.0, 0.5, 90.0f, 0.0f));
        Players.giveHubItems(victim);

        String currentEventName = EventsMain.getEventName();
        if (currentEventName.equals("末地")) {
            if (victim.getMaxHealth() > 20.0) {
                End.addMaxHealth(victim, -2.0);
                victim.sendMessage("§c[❤] 你死亡了！生命上限减少了！");
            }
            if (killer != null) {
                End.addMaxHealth(killer, 2.0);
                killer.sendMessage("§c[❤] §a你击杀了一名玩家，生命上限增加了！");
            }
        }
        if (currentEventName.equals("末影龙")) {
            victim.setMaxHealth(20.0);
            if (killer != null) {
                Dragon.setDragonPlayer(killer);
            } else {
                Dragon.setDragonPlayer(Dragon.selectRandomValidPlayer());
            }
        }
        if (currentEventName.equals("疯狂")) {
            if (killer != null) {
                Crazy.addKill(killer);
            }
        }
        if (currentEventName.equals("团队")) {
            if (killer != null && Team.getTeam(killer) != null) {
                Team.addKill(killer);
            }
        }
    }

    private static void playKillSound(Player killer, Player victim) {
        Sound killerSound, victimSound;
        String job = Players.getJobFromPlayer(killer);
        if (job.equals("?")) return;
        victimSound = switch (job) {
            case "烈焰人" -> Sound.ENTITY_BLAZE_AMBIENT;
            case "旋风人" -> Sound.ENTITY_BREEZE_IDLE_AIR;
            case "苦力怕" -> Sound.ENTITY_CREEPER_DEATH;
            case "末影人" -> Sound.ENTITY_ENDERMAN_AMBIENT;
            case "唤魔者" -> Sound.ENTITY_EVOKER_PREPARE_WOLOLO;
            case "恶魂" -> Sound.ENTITY_GHAST_SCREAM;
            case "山羊" -> Sound.ENTITY_GOAT_SCREAMING_AMBIENT;
            case "守卫者" -> Sound.ENTITY_GUARDIAN_AMBIENT;
            case "幻术师" -> Sound.ENTITY_ILLUSIONER_AMBIENT;
            case "猪灵" -> Sound.ENTITY_PIGLIN_CELEBRATE;
            case "骷髅" -> Sound.ENTITY_SKELETON_CONVERTED_TO_STRAY;
            case "嗅探兽" -> Sound.ENTITY_SNIFFER_HAPPY;
            case "鱿鱼" -> Sound.ENTITY_SQUID_DEATH;
            case "流浪商人" -> Sound.ENTITY_WANDERING_TRADER_YES;
            case "卫道士" -> Sound.ENTITY_VINDICATOR_CELEBRATE;
            case "监守者" -> Sound.ENTITY_WARDEN_DIG;
            case "凋灵" -> Sound.ENTITY_WITHER_AMBIENT;
            case "溺尸" -> Sound.ENTITY_DROWNED_DEATH;
            default -> null;
        };
        killerSound = switch (job) {
            case "旋风人" -> Sound.ENTITY_BREEZE_DEFLECT;
            case "唤魔者" -> Sound.ENTITY_EVOKER_CELEBRATE;
            case "猪灵" -> Sound.ENTITY_PIGLIN_CELEBRATE;
            case "卫道士" -> Sound.ENTITY_VINDICATOR_CELEBRATE;
            default -> null;
        };
        if (victimSound != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                victim.playSound(victim.getLocation(), victimSound, 1.0f, 1.0f);
            }, 4L);
        }
        if (killerSound != null) {
            killer.playSound(killer.getLocation(), victimSound, 1.0f, 1.0f);
        }
    }

    private static void playDeathSound(Player victim) {
        Sound victimSound;
        String job = Players.getJobFromPlayer(victim);
        if (job.equals("?")) return;
        victimSound = switch (job) {
            case "烈焰人" -> Sound.ENTITY_BLAZE_DEATH;
            case "旋风人" -> Sound.ENTITY_BREEZE_DEATH;
            case "苦力怕" -> Sound.ENTITY_CREEPER_DEATH;
            case "末影人" -> Sound.ENTITY_ENDERMAN_DEATH;
            case "唤魔者" -> Sound.ENTITY_EVOKER_DEATH;
            case "恶魂" -> Sound.ENTITY_GHAST_DEATH;
            case "山羊" -> Sound.ENTITY_GOAT_SCREAMING_DEATH;
            case "守卫者" -> Sound.ENTITY_GUARDIAN_DEATH;
            case "幻术师" -> Sound.ENTITY_ILLUSIONER_DEATH;
            case "猪灵" -> Sound.ENTITY_PIGLIN_DEATH;
            case "骷髅" -> Sound.ENTITY_SKELETON_DEATH;
            case "嗅探兽" -> Sound.ENTITY_SNIFFER_DEATH;
            case "鱿鱼" -> Sound.ENTITY_SQUID_DEATH;
            case "流浪商人" -> Sound.ENTITY_WANDERING_TRADER_DEATH;
            case "卫道士" -> Sound.ENTITY_VINDICATOR_DEATH;
            case "监守者" -> Sound.ENTITY_WARDEN_DEATH;
            case "凋灵" -> Sound.ENTITY_WITHER_DEATH;
            case "溺尸" -> Sound.ENTITY_DROWNED_AMBIENT_WATER;
            default -> throw new IllegalStateException("Unexpected value: " + job);
        };
        victim.getWorld().playSound(victim.getLocation(), victimSound, 1.0f, 1.0f);
    }

    private void spawnFirework(Player player) {
        Color color = Jobs.getJobColorName(player);
        Firework firework = player.getWorld().spawn(player.getLocation().add(0.0, 2.0, 0.0), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder().withColor(color).with(FireworkEffect.Type.BALL).trail(false).flicker(false).build());
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
        firework.setMetadata("noDamage", new FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!firework.isDead()) {
                firework.detonate();
            }
        }, 1L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        if (EventsMain.getEventName().equals("刷怪笼")) {
            UUID killerUUID = killer.getUniqueId();
            event.getDrops().clear();
            event.setDroppedExp(0);
            String customName = event.getEntity().getCustomName();
            if (customName == null) {
                return;
            }
            int amount = extractNumber(customName);
            if (amount == -1) {
                return;
            }
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            int kills = Spawner.spawnerEventKills.getOrDefault(killerUUID, 0);
            Spawner.spawnerEventKills.put(killerUUID, kills + 1);
            switch (event.getEntity().getType()) {
                case ZOMBIE:
                case PILLAGER:
                    Emerald.dropEmerald(event.getEntity().getLocation(), amount);
                    break;
                case VINDICATOR:
                    Energy.addEnergyToAllBars(killer, amount);
                    break;
                case HOGLIN:
                case PIGLIN_BRUTE:
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), Items.createGoldIngot(amount));
                    break;
                case WITCH:
                    addOrExtendPotionEffect(killer, PotionEffectType.JUMP_BOOST, amount * 20);
                    addOrExtendPotionEffect(killer, PotionEffectType.SPEED, amount * 20);
                    addOrExtendPotionEffect(killer, PotionEffectType.RESISTANCE, amount * 20);
                    addOrExtendPotionEffect(killer, PotionEffectType.STRENGTH, amount * 20);
                    addOrExtendPotionEffect(killer, PotionEffectType.REGENERATION, amount * 20);
                    addOrExtendPotionEffect(killer, PotionEffectType.ABSORPTION, amount * 20);
            }
        }
    }

    private int extractNumber(String str) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1;
    }

    private void addOrExtendPotionEffect(Player player, PotionEffectType effectType, int duration) {
        PotionEffect currentEffect = player.getPotionEffect(effectType);
        if (currentEffect != null) {
            int newDuration = currentEffect.getDuration() + duration;
            player.addPotionEffect(new PotionEffect(effectType, newDuration, currentEffect.getAmplifier()));
        } else {
            player.addPotionEffect(new PotionEffect(effectType, duration, 0));
        }
    }
}
