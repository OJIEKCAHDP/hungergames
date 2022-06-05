package dev.ojiekcahdp.hungergames.game.listeners;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.utils.Utils;
import lombok.Setter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dreammine.lib.dreamlib.platform.adapter.CrossCommandSender;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class EnderChestListener implements Listener {

    private final HashMap<EnderChest, Inventory> chests = new HashMap<>();
    private final HashMap<EnderChest, Integer> itemsCount = new HashMap<>();
    private final Main plugin = Main.getInstance();
    private @Setter
    int timer = 0;

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getState() instanceof EnderChest enderChest) {

                e.setCancelled(true);

                if (timer > 0) {

                    e.getPlayer().sendActionBar(Component.text(
                            plugin.getLanguageAccessor().translate(
                                    CrossCommandSender.bukkit(e.getPlayer()), "game.actionbar.enderchest")
                                    .replace("%time", Utils.getTimeString(timer))
                    ));

                    return;
                }

                if (!chests.containsKey(enderChest)) {
                    Inventory inv = Bukkit.createInventory(null, InventoryType.ENDER_CHEST);
                    itemsCount.put(enderChest,
                            plugin.getArena().setChestLoot(Utils.getLoot(plugin.getLoot(), "ender-chest.items"), inv, null)
                                    .size());
                    chests.put(enderChest, inv);
                }

                Inventory inventory = chests.get(enderChest);

                e.getPlayer().openInventory(inventory);

                int size = itemsCount.size();

                if (size == 0) {
                    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Teleport");
                    npc.spawn(enderChest.getLocation().add(0, 1, 0));
                    plugin.getArena().getNpcList().add(npc);
                }

            }
        }
    }

    public void startTimer() {
        this.timer = plugin.getConfig().getInt("ender-chest-open-time");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (timer == 0) {
                    cancel();
                }
                timer--;
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20);

    }

}
