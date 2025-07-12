package org.windguest.mobwar.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.windguest.mobwar.Main;
import org.windguest.mobwar.games.Players;

public class ListenerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = event.getPlayer().getName();
        event.quitMessage(Component.text("[-] ", NamedTextColor.RED)
                .append(Component.text(playerName, NamedTextColor.RED)));
        Players.clearPlayerProjectiles(event.getPlayer());
        if (player.hasMetadata("last_attacker")) {
            player.removeMetadata("last_attacker", Main.getInstance());
        }
        Players.clearPlayerAllData(player);
    }
}
