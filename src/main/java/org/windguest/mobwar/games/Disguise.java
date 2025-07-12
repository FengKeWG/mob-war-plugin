package org.windguest.mobwar.games;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.windguest.mobwar.events.EventsMain;
import org.windguest.mobwar.events.Team;
import org.windguest.mobwar.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Disguise implements Listener {

    private static final Main plugin = Main.getInstance();
    private static final Map<UUID, Integer> disguiseCount = new HashMap<>();

    public static void disguise(Player player, int type) {
        String name = Players.getPlayerPrefix(player) + " " + player.getName();
        String job = Players.getJobFromPlayer(player);
        MobDisguise disguise = null;
        switch (job) {
            case "烈焰人":
                disguise = new MobDisguise(DisguiseType.BLAZE);
                if (type == 1) {
                    BlazeWatcher blazeWatcher = (BlazeWatcher) disguise.getWatcher();
                    blazeWatcher.setBlazing(true);
                }
                break;
            case "旋风人":
                disguise = new MobDisguise(DisguiseType.BREEZE);
                break;
            case "苦力怕":
                disguise = new MobDisguise(DisguiseType.CREEPER);
                if (type == 1) {
                    CreeperWatcher creeperWatcher = (CreeperWatcher) disguise.getWatcher();
                    creeperWatcher.setPowered(true);
                }
                break;
            case "末影人":
                disguise = new MobDisguise(DisguiseType.ENDERMAN);
                EndermanWatcher endermanWatcher = (EndermanWatcher) disguise.getWatcher();
                endermanWatcher.setEnraged(true);
                break;
            case "唤魔者":
                disguise = new MobDisguise(DisguiseType.EVOKER);
                break;
            case "恶魂":
                disguise = new MobDisguise(DisguiseType.GHAST);
                break;
            case "山羊":
                disguise = new MobDisguise(DisguiseType.GOAT);
                break;
            case "守卫者":
                disguise = new MobDisguise(DisguiseType.GUARDIAN);
                if (type == 1) {
                    disguise = new MobDisguise(DisguiseType.ELDER_GUARDIAN);
                }
                break;
            case "幻术师":
                disguise = new MobDisguise(DisguiseType.ILLUSIONER);
                break;
            case "猪灵":
                disguise = new MobDisguise(DisguiseType.PIGLIN);
                if (type == 1) {
                    disguise = new MobDisguise(DisguiseType.PIGLIN_BRUTE);
                } else if (type == 2) {
                    PiglinWatcher piglinWatcher = (PiglinWatcher) disguise.getWatcher();
                    piglinWatcher.setBaby(true);
                }
                break;
            case "骷髅":
                if (type == 0) {
                    disguise = new MobDisguise(DisguiseType.SKELETON);
                } else if (type == 1) {
                    disguise = new MobDisguise(DisguiseType.STRAY);
                } else if (type == 2) {
                    disguise = new MobDisguise(DisguiseType.BOGGED);
                }
                break;
            case "嗅探兽":
                disguise = new MobDisguise(DisguiseType.SNIFFER);
                break;
            case "鱿鱼":
                disguise = new MobDisguise(DisguiseType.GLOW_SQUID);
                break;
            case "流浪商人":
                disguise = new MobDisguise(DisguiseType.WANDERING_TRADER);
                break;
            case "卫道士":
                disguise = new MobDisguise(DisguiseType.VINDICATOR);
                VindicatorWatcher vindicatorWatcher = (VindicatorWatcher) disguise.getWatcher();
                vindicatorWatcher.setEnraged(true);
                break;
            case "监守者":
                disguise = new MobDisguise(DisguiseType.WARDEN);
                break;
            case "凋灵":
                disguise = new MobDisguise(DisguiseType.WITHER);
                break;
            case "溺尸":
                disguise = new MobDisguise(DisguiseType.DROWNED);
                break;
            default:
                return;
        }
        if (disguise == null) return;
        DisguiseAPI.disguiseToAll(player, disguise);
        LivingWatcher watcher = disguise.getWatcher();
        watcher.setCustomName(name);
        watcher.setCustomNameVisible(true);
        addDisguiseCount(player);
        if (player.hasPotionEffect(PotionEffectType.GLOWING)) {
            setGlow(player, true);
        }
    }

    public static void disguiseAction(Player player) {
        String job = Players.getJobFromPlayer(player);
        if (!DisguiseAPI.isDisguised(player)) return;
        MobDisguise disguise = (MobDisguise) DisguiseAPI.getDisguise(player);
        switch (job) {
            case "恶魂":
                GhastWatcher ghastWatcher = (GhastWatcher) disguise.getWatcher();
                ghastWatcher.setAggressive(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ghastWatcher.setAggressive(false);
                }, 15L);
                break;
        }
    }

    public static void cancelGlow(Player player) {
        if (DisguiseAPI.isDisguised(player)) {
            MobDisguise disguise = (MobDisguise) DisguiseAPI.getDisguise(player);
            LivingWatcher watcher = disguise.getWatcher();
            watcher.setGlowing(false);
        }
    }

    public static void undisguise(Player player) {
        if (getDisguiseCount(player) <= 1) {
            DisguiseAPI.undisguiseToAll(player);
        }
        reduceDisguiseCount(player);
    }

    private static void addDisguiseCount(Player player) {
        UUID uuid = player.getUniqueId();
        disguiseCount.put(uuid, disguiseCount.getOrDefault(uuid, 0) + 1);
    }

    private static void reduceDisguiseCount(Player player) {
        UUID uuid = player.getUniqueId();
        disguiseCount.put(uuid, Math.max(0, disguiseCount.getOrDefault(uuid, 0) - 1));
    }

    private static int getDisguiseCount(Player player) {
        return disguiseCount.getOrDefault(player.getUniqueId(), 0);
    }

    public static void clearDisguiseCount(Player player) {
        disguiseCount.remove(player.getUniqueId());
    }

    @EventHandler
    public static void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!DisguiseAPI.isDisguised(player)) return;
        if (event.getModifiedType() == PotionEffectType.GLOWING) {
            if (event.getAction() == EntityPotionEffectEvent.Action.ADDED) {
                setGlow(player, true);
            } else if (event.getAction() == EntityPotionEffectEvent.Action.REMOVED
                    || event.getAction() == EntityPotionEffectEvent.Action.CLEARED) {
                setGlow(player, false);
            }
        }
    }

    private static void setGlow(Player player, boolean glowing) {
        if (!DisguiseAPI.isDisguised(player)) return;
        MobDisguise disguise = (MobDisguise) DisguiseAPI.getDisguise(player);
        LivingWatcher watcher = disguise.getWatcher();
        if (glowing) {
            ChatColor glowColor = convertColorToChatColor(Jobs.getJobColorName(player));
            if (EventsMain.getEventName().equals("团队")) {
                if (Team.getTeam(player).equals("红队")) {
                    glowColor = ChatColor.RED;
                } else if (Team.getTeam(player).equals("蓝队")) {
                    glowColor = ChatColor.BLUE;
                }
            }
            watcher.setGlowing(true);
            watcher.setGlowColor(glowColor);
        } else {
            watcher.setGlowing(false);
        }
    }

    private static ChatColor convertColorToChatColor(Color color) {
        if (color.equals(Color.BLACK)) return ChatColor.BLACK;
        if (color.equals(Color.BLUE)) return ChatColor.DARK_BLUE;
        if (color.equals(Color.GREEN)) return ChatColor.DARK_GREEN;
        if (color.equals(Color.TEAL)) return ChatColor.DARK_AQUA;
        if (color.equals(Color.RED)) return ChatColor.DARK_RED;
        if (color.equals(Color.PURPLE)) return ChatColor.DARK_PURPLE;
        if (color.equals(Color.ORANGE)) return ChatColor.GOLD;
        if (color.equals(Color.SILVER)) return ChatColor.GRAY;
        if (color.equals(Color.GRAY)) return ChatColor.DARK_GRAY;
        if (color.equals(Color.LIME)) return ChatColor.GREEN;
        if (color.equals(Color.AQUA)) return ChatColor.AQUA;
        if (color.equals(Color.FUCHSIA)) return ChatColor.LIGHT_PURPLE;
        if (color.equals(Color.YELLOW)) return ChatColor.YELLOW;
        return ChatColor.WHITE;
    }
}
