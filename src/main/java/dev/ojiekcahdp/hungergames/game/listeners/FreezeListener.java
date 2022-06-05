package dev.ojiekcahdp.hungergames.game.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {

    @EventHandler
    public void on(PlayerMoveEvent e) {
        e.setCancelled(true);
    }

}
