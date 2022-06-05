package dev.ojiekcahdp.hungergames.game.listeners;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.utils.Utils;
import net.citizensnpcs.api.event.NPCClickEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCListener implements Listener {

    private final Main plugin = Main.getInstance();

    private final Location location = Utils.getLocationFromString(plugin.getConfig().getString("npc-teleport-location"));

    @EventHandler
    public void on(NPCClickEvent e) {
        if (plugin.getArena().getNpcList().contains(e.getNPC())) {
            e.getClicker().teleport(this.location);
            e.getNPC().despawn();
        }
    }

}
