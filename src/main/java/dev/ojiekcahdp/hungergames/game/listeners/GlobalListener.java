package dev.ojiekcahdp.hungergames.game.listeners;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.game.Arena;
import dev.ojiekcahdp.hungergames.game.entity.GameEntity;
import dev.ojiekcahdp.hungergames.game.enums.GameState;
import dev.ojiekcahdp.hungergames.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class GlobalListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final Arena arena = plugin.getArena();
    private final Location spawnLocation = Utils.getLocationFromString(plugin.getConfig().getString("waiting-lobby"));

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerSpawnLocationEvent e) {
        e.setSpawnLocation(this.spawnLocation);
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        e.joinMessage(null);
        e.getPlayer().setGameMode(GameMode.SURVIVAL);
        e.getPlayer().setHealth(20);
        e.getPlayer().setFoodLevel(20);
        e.getPlayer().setSaturation(20);
        arena.addPlayer(new GameEntity(e.getPlayer()));
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        e.quitMessage(null);
        arena.removePlayer(GameEntity.fromPlayer(e.getPlayer()));
    }

    @EventHandler
    public void on(PlayerDeathEvent e) {
        e.deathMessage(null);
        if (arena.getState() == GameState.STARTED) {
            GameEntity entity = GameEntity.fromPlayer(e.getEntity());
            arena.onPlayerDeath(entity);
            arena.getAlivePlayers().remove(entity);
            if (arena.getAlivePlayers().size() <= 1) arena.onGameEnded();
        }
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        e.setCancelled(!Tag.LEAVES.isTagged(e.getBlock().getType()));
    }

    @EventHandler
    public void on(BlockPlaceEvent e) {
        e.setCancelled(!Tag.LEAVES.isTagged(e.getBlock().getType()));
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            Material material = e.getClickedBlock().getType();
            e.setCancelled(!Tag.BUTTONS.isTagged(material) &&
                    material != Material.CHEST &&
                    material != Material.ENDER_CHEST &&
                    !Tag.LEAVES.isTagged(material) &&
                    !Tag.DOORS.isTagged(material));
        }
    }

}
