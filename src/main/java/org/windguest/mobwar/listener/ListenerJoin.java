package org.windguest.mobwar.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.windguest.mobwar.games.Energy;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;
import org.windguest.mobwar.events.*;

public class ListenerJoin implements Listener {

    private static final Main plugin = Main.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        player.setGameMode(GameMode.ADVENTURE);
        event.joinMessage(Component.text("[+] ", NamedTextColor.GREEN)
                .append(Component.text(playerName, NamedTextColor.GREEN)));
        player.teleport(new Location(Bukkit.getWorld("world"), -14.5, 243.0, 0.5, 90, 0));
        Players.clearPlayerAllData(player);
        Players.giveHubItems(player);
        player.setMaxHealth(20.0);
        if (EventsMain.getEventName().equals("团队")) {
            String team = Team.getTeam(player);
            if (team != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false, false));
                if (team.equals("红队")) {
                    player.setPlayerListName("§c" + player.getName());
                } else {
                    player.setPlayerListName("§9" + player.getName());
                }
            } else {
                Team.assignTeam(player);
            }
        }
        new BukkitRunnable() {
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                if (isInArea(loc)) {
                    Energy.addEnergyToAllBars(player, 1);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
        if (EventsMain.getEventName().equals("末影龙") && Dragon.getDragonPlayer() == null) {
            Dragon.setDragonPlayer(Dragon.selectRandomValidPlayer());
        }
    }

    private boolean isInArea(Location loc) {
        return loc.getX() >= Math.min(-3, 4) && loc.getX() <= Math.max(-3, 4)
                && loc.getY() == 30
                && loc.getZ() >= Math.min(4, -3) && loc.getZ() <= Math.max(4, -3);
    }
}
