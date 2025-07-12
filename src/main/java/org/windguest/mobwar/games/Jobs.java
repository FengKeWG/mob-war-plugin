package org.windguest.mobwar.games;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.windguest.mobwar.events.EventsMain;
import org.windguest.mobwar.events.*;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.mobs.*;

import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Jobs {

    private static final Main plugin = Main.getInstance();
    private static final World world = Bukkit.getWorld("world");

    private static final Random random = new Random();

    private static final Location[] LOCATIONS = new Location[]{
            new Location(world, 27.5, 53, 10.5),
            new Location(world, -24.5, 49, 15.5),
            new Location(world, -0.5, 30, -22.5),
            new Location(world, 49.5, 57, -0.5),
            new Location(world, 32.5, 56, 43.5),
            new Location(world, -17.5, 64, 46.5),
            new Location(world, -47.5, 56, 26.5),
            new Location(world, -50.5, 61, -23.5),
            new Location(world, -20.5, 65, -54.5),
            new Location(world, 81.5, 87, -25.5),
            new Location(world, 98.5, 87, 14.5),
            new Location(world, 55.5, 87, 79.5),
            new Location(world, -1.5, 90, 90.5),
            new Location(world, -39.5, 102, 79.5),
            new Location(world, -61.5, 87, 51.5),
            new Location(world, -19.5, 87, -85.5),
            new Location(world, 46.5, 90, -78.5),
            new Location(world, -24.5, 54, -5.5),
            new Location(world, 10.5, 30, 29.5),
            new Location(world, -99.5, 87.5, -20.5)
    };

    private static final Map<String, String> JOB_ICONS = Map.ofEntries(
            Map.entry("?", "🚫"),
            Map.entry("烈焰人", "🔥"),
            Map.entry("苦力怕", "💣"),
            Map.entry("末影人", "🧿"),
            Map.entry("唤魔者", "👹"),
            Map.entry("恶魂", "👻"),
            Map.entry("山羊", "🐐"),
            Map.entry("幻术师", "🧙"),
            Map.entry("猪灵", "🐷"),
            Map.entry("鱿鱼", "🦑"),
            Map.entry("流浪商人", "📿"),
            Map.entry("卫道士", "🪓"),
            Map.entry("监守者", "🎵"),
            Map.entry("凋灵", "🖤"),
            Map.entry("旋风人", "🌀"),
            Map.entry("骷髅", "💀"),
            Map.entry("守卫者", "🐟"),
            Map.entry("嗅探兽", "🌸"),
            Map.entry("溺尸", "🔱")
    );

    private static final Map<String, String> JOB_COLOR_CODES = Map.ofEntries(
            Map.entry("?", "§7"),
            Map.entry("烈焰人", "§6"),
            Map.entry("苦力怕", "§2"),
            Map.entry("末影人", "§5"),
            Map.entry("唤魔者", "§9"),
            Map.entry("恶魂", "§f"),
            Map.entry("山羊", "§f"),
            Map.entry("幻术师", "§9"),
            Map.entry("猪灵", "§e"),
            Map.entry("鱿鱼", "§1"),
            Map.entry("流浪商人", "§b"),
            Map.entry("卫道士", "§8"),
            Map.entry("监守者", "§1"),
            Map.entry("凋灵", "§0"),
            Map.entry("旋风人", "§b"),
            Map.entry("骷髅", "§f"),
            Map.entry("守卫者", "§3"),
            Map.entry("嗅探兽", "§2"),
            Map.entry("溺尸", "§3")
    );

    // private static final Map<String, Material> JOB_MATERIALS = Map.ofEntries(
    //         Map.entry("烈焰人", Material.BLAZE_ROD),
    //         Map.entry("苦力怕", Material.TNT),
    //         Map.entry("末影人", Material.ENDER_PEARL),
    //         Map.entry("幻术师", Material.FLETCHING_TABLE),
    //         Map.entry("卫道士", Material.IRON_AXE),
    //         Map.entry("恶魂", Material.GHAST_TEAR),
    //         Map.entry("山羊", Material.GOAT_HORN),
    //         Map.entry("流浪商人", Material.LEAD),
    //         Map.entry("监守者", Material.ECHO_SHARD),
    //         Map.entry("鱿鱼", Material.INK_SAC),
    //         Map.entry("猪灵", Material.GOLD_INGOT),
    //         Map.entry("唤魔者", Material.TOTEM_OF_UNDYING),
    //         Map.entry("凋灵", Material.WITHER_SKELETON_SKULL),
    //         Map.entry("旋风人", Material.WIND_CHARGE),
    //         Map.entry("骷髅", Material.BOW),
    //         Map.entry("守卫者", Material.PRISMARINE_SHARD),
    //         Map.entry("嗅探兽", Material.BRUSH)
    // );

    // 返回所有可用的职业名称列表（不包含 “?”）
    public static List<String> getAllJobNames() {
        List<String> list = new ArrayList<>(JOB_COLOR_CODES.keySet());
        list.remove("?");
        return list;
    }

    public static void startGame(Player player, String job, boolean tp) {
        if (!Players.getJobFromPlayer(player).equals("?")) {
            return;
        }
        Players.addJobToPlayer(player, job);
        Disguise.undisguise(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        Players.removePlayerInHole(player);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0f, 1.0f);
        }, 4L);
        if (Edit.hasEdited(player, job)) {
            Edit.loadPlayerEditedInventory(player, job);
        } else {
            equipKits(player, job);
        }
        String currentEventName = EventsMain.getEventName();
        if (currentEventName.equals("空岛")) {
            Sky.teleportToSky(player);
        } else if (currentEventName.equals("末地")) {
            if (tp) {
                End.teleportToEnd(player);
            }
        } else {
            if (tp) {
                Location randomLocation = LOCATIONS[random.nextInt(LOCATIONS.length)];
                player.teleport(randomLocation);
            } else {
                player.setVelocity(new Vector(0, -100, 0));
            }
        }
        switch (job) {
            case "烈焰人":
                Blaze.start(player);
                break;
            case "旋风人":
                Breeze.start(player);
                break;
            case "苦力怕":
                Creeper.start(player);
                break;
            case "末影人":
                EnderMan.start(player);
                break;
            case "唤魔者":
                Evoker.start(player);
                break;
            case "恶魂":
                Ghast.start(player);
                break;
            case "山羊":
                Goat.start(player);
                break;
            case "守卫者":
                Guardian.start(player);
                break;
            case "幻术师":
                Illusioners.start(player);
                break;
            case "猪灵":
                Piglin.start(player);
                break;
            case "骷髅":
                Skeletons.start(player);
                break;
            case "嗅探兽":
                Sniffer.start(player);
                break;
            case "鱿鱼":
                Squid.start(player);
                break;
            case "流浪商人":
                Trader.start(player);
                break;
            case "卫道士":
                Vindicator.start(player);
                break;
            case "监守者":
                Warden.start(player);
                break;
            case "凋灵":
                Wither.start(player);
                break;
            case "溺尸":
                Drowned.start(player);
                break;
        }
    }

    public static void equipKits(Player player, String job) {
        switch (job) {
            case "烈焰人":
                Blaze.equipKits(player);
                break;
            case "旋风人":
                Breeze.equipKits(player);
                break;
            case "苦力怕":
                Creeper.equipKits(player);
                break;
            case "末影人":
                EnderMan.equipKits(player);
                break;
            case "唤魔者":
                Evoker.equipKits(player);
                break;
            case "恶魂":
                Ghast.equipKits(player);
                break;
            case "山羊":
                Goat.equipKits(player);
                break;
            case "守卫者":
                Guardian.equipKits(player);
                break;
            case "幻术师":
                Illusioners.equipKits(player);
                break;
            case "猪灵":
                Piglin.equipKits(player);
                break;
            case "骷髅":
                Skeletons.equipKits(player);
                break;
            case "嗅探兽":
                Sniffer.equipKits(player);
                break;
            case "鱿鱼":
                Squid.equipKits(player);
                break;
            case "流浪商人":
                Trader.equipKits(player);
                break;
            case "卫道士":
                Vindicator.equipKits(player);
                break;
            case "监守者":
                Warden.equipKits(player);
                break;
            case "凋灵":
                Wither.equipKits(player);
                break;
            case "溺尸":
                Drowned.equipKits(player);
                break;
        }
    }

    public static String getJobColorCode(Player player) {
        String job = Players.getJobFromPlayer(player);
        return JOB_COLOR_CODES.get(job);
    }

    public static String getJobIcon(Player player) {
        String job = Players.getJobFromPlayer(player);
        return JOB_ICONS.get(job);
    }

    public static Color getJobColorName(Player player) {
        String job = Players.getJobFromPlayer(player);
        String colorCode = JOB_COLOR_CODES.get(job);
        char color = colorCode.charAt(1);
        return switch (color) {
            case '0' -> Color.BLACK;
            case '1', '9' -> Color.BLUE;
            case '2' -> Color.GREEN;
            case '3' -> Color.TEAL;
            case '4', 'c' -> Color.RED;
            case '5' -> Color.PURPLE;
            case '6' -> Color.ORANGE;
            case '7' -> Color.SILVER;
            case '8' -> Color.GRAY;
            case 'a' -> Color.LIME;
            case 'b' -> Color.AQUA;
            case 'd' -> Color.FUCHSIA;
            case 'e' -> Color.YELLOW;
            default -> Color.WHITE;
        };
    }
}
