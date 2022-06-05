package dev.ojiekcahdp.hungergames.game.entity;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class GameEntity {

    private static final HashMap<Player, GameEntity> registeredEntities = new HashMap<>();

    private final Player player;
    private int kills;
    private boolean killed;
    private Location spawnLocation;
    private @Setter boolean winner = false;
    private final List<String> availableKits;
    private @Setter String selectedKit = "default";

    public GameEntity(Player player) {
        this.player = player;
        this.availableKits = new ArrayList<>();
        registeredEntities.put(player, this);
    }

    public void incKill() {
        this.kills++;
    }

    public void incKills(int kills) {
        this.kills+=kills;
    }

    public void death() {
        this.player.setGameMode(GameMode.SPECTATOR);
        this.killed = true;
    }

    public void teleport(Location location) {
        this.spawnLocation = location;
        this.player.teleport(location);
    }

    public boolean giveKit(String donate) {
        File file = new File(Main.getInstance().getDataFolder(), "/kits/" + donate + ".yml");
        if (!file.exists()) return false;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        int slot = 0;
        for (ItemStack itemStack : Utils.deserializeItems(yaml.getString("items"))) {
            slot++;
            if (itemStack == null) continue;
            player.getInventory().setItem(slot, itemStack);
        }
        return true;
    }

    public static GameEntity fromPlayer(Player player) {
        return registeredEntities.get(player);
    }

}
