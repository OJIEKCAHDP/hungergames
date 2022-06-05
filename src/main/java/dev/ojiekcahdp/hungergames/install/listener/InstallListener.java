package dev.ojiekcahdp.hungergames.install.listener;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.utils.Utils;
import dev.ojiekcahdp.hungergames.utils.region.Cuboid;
import net.dreammine.lib.dreamlib.language.LanguageAPI;
import net.dreammine.lib.dreamlib.platform.adapter.CrossCommandSender;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstallListener implements Listener {

    private final Main plugin;
    private final LanguageAPI.LanguageAccessor accessor;

    private int state = 0;
    private final List<String> locations = new ArrayList<>();

    public InstallListener(Main plugin, LanguageAPI.LanguageAccessor accessor) {
        this.plugin = plugin;
        this.accessor = accessor;
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (e.getClickedBlock() != null) {
                CrossCommandSender sender = CrossCommandSender.bukkit(e.getPlayer());
                if (e.getItem().isSimilar(plugin.getInstallCommand().getArenaItem())) {
                    switch (state) {
                        case 0 -> {
                            state++;
                            plugin.getConfig().set("arena.first-position", Utils.getStringFromLocation(e.getClickedBlock().getLocation()));

                            e.getPlayer().sendMessage(Component.text(accessor.translate(sender, "arena.select.first")));
                        }
                        case 1 -> {
                            state = 0;
                            plugin.getConfig().set("arena.second-position", Utils.getStringFromLocation(e.getClickedBlock().getLocation()));

                            e.getPlayer().sendMessage(Component.text(accessor.translate(sender, "arena.select.second")));
                            e.getItem().setAmount(0);

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                Location first = Utils.getLocationFromString(plugin.getConfig().getString("arena.first-position"));
                                assert first != null;
                                Cuboid cuboid = new Cuboid(first, e.getClickedBlock().getLocation());

                                List<String> locations = cuboid.findChests().stream()
                                        .map(BlockState::getLocation)
                                        .map(Utils::getStringFromLocation)
                                        .collect(Collectors.toList());
                                plugin.getConfig().set("chests", locations);

                                locations = cuboid.findEnderChests().stream()
                                        .map(BlockState::getLocation)
                                        .map(Utils::getStringFromLocation)
                                        .collect(Collectors.toList());
                                plugin.getConfig().set("ender-chests", locations);

                            });

                        }
                    }
                    plugin.saveConfig();
                } else if (e.getItem().isSimilar(plugin.getInstallCommand().getLocationItem())) {

                    locations.add(Utils.getStringFromLocation(e.getClickedBlock().getLocation().add(0, 1, 0)));
                    plugin.getConfig().set("spawn-locations", locations);

                    e.getPlayer().sendMessage(Component.text("Total locations: " + locations.size()));
                    plugin.saveConfig();
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        CrossCommandSender sender = CrossCommandSender.bukkit(e.getPlayer());

        player.sendMessage(Component.text(plugin.getLanguageAccessor().translate(sender, "arena.not.configured")
                .replace("%reason", plugin.getSetupReason().toString())
        ));
    }

}
