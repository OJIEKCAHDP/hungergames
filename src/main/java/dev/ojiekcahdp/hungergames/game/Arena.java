package dev.ojiekcahdp.hungergames.game;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.game.entity.GameEntity;
import dev.ojiekcahdp.hungergames.game.enums.GameState;
import dev.ojiekcahdp.hungergames.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.dreammine.lib.dreamlib.language.LanguageAPI;
import net.dreammine.lib.dreamlib.platform.adapter.CrossCommandSender;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public abstract class Arena {

    private final Main plugin = Main.getInstance();

    private final String map;
    private final World world;
    private GameState state;
    private final int waitingTime;
    private final int damageCooldown;
    private int timer;
    private final List<Chest> chests;
    private final List<GameEntity> players;
    private List<GameEntity> alivePlayers;
    private final List<Location> spawnLocations;
    private @Setter
    LanguageAPI.LanguageAccessor accessor;

    public Arena(String map, int waitingTime, int damageCooldown) {
        this.map = map;
        this.state = GameState.WAITING_FOR_PLAYERS;
        this.waitingTime = waitingTime;
        this.damageCooldown = damageCooldown;
        this.chests = Utils.getChestList(plugin.getConfig().getStringList("chests"));
        this.players = new ArrayList<>();
        this.spawnLocations = Utils.getLocationListFromStringList(plugin.getConfig().getStringList("spawn-locations"));
        this.world = spawnLocations.get(0).getWorld();
    }

    public abstract void onGameStart();

    public abstract void onGameEnded();

    public abstract void onPlayerJoin(Player player);

    public abstract void onPlayerDeath(GameEntity gameEntity);

    public void onStartWaitingTimer() {
    }

    public void startWaitingTimer() {
        this.state = GameState.WAITING;
        Bukkit.getPluginManager().registerEvents(Main.getInstance().getFreezeListener(), Main.getInstance());
        spawnPlayers();
        startTimer(this.waitingTime);
        onStartWaitingTimer();
    }

    public void start() {
        this.state = GameState.STARTED;

        HandlerList.unregisterAll(plugin.getFreezeListener());

        startTimer(this.damageCooldown);

        this.alivePlayers = new ArrayList<>(this.players);
        onGameStart();
    }

    public void addPlayer(GameEntity entity) {
        this.players.add(entity);
        onPlayerJoin(entity.getPlayer());
        if (this.state == GameState.WAITING)
            entity.teleport(getRandomLocation());
        if (this.state == GameState.WAITING_FOR_PLAYERS) {
            if (this.players.size() == 2) {
                this.startWaitingTimer();
            }
        }
    }

    public void removePlayer(GameEntity entity) {
        this.players.remove(entity);
        if (this.state == GameState.STARTED) {
            entity.death();
            this.alivePlayers.remove(entity);
            if (this.alivePlayers.size() == 1) {
                this.state = GameState.ENDED;
                onGameEnded();
            }
        } else if (this.players.size() == 1) {
            this.timer = 0;
            this.state = GameState.WAITING_FOR_PLAYERS;
        }
        if (this.state == GameState.WAITING) {
            this.availableLocations.add(entity.getSpawnLocation());
        }
    }

    public void onTimerChange() {
        if (this.state == GameState.STARTED) {
            switch (this.timer) {
                case 15, 10, 5, 4, 3, 2, 1 -> this.players.forEach(entity -> {
                    Player player = entity.getPlayer();
                    CrossCommandSender sender = CrossCommandSender.bukkit(player);
                    String title = accessor.translate(sender, "game.title.prepare-to-fight.title").replace("%time", timer + "");
                    String subtitle = accessor.translate(sender, "game.title.prepare-to-fight.subtitle").replace("%time", timer + "");
                    entity.getPlayer().sendTitle(title, subtitle, 5, 10, 5);
                });
            }
            if (this.timer == 0) {
                this.players.forEach(entity -> {
                    Player player = entity.getPlayer();
                    CrossCommandSender sender = CrossCommandSender.bukkit(player);
                    String title = accessor.translate(sender, "game.title.prepare-to-fight-goodLuck.title");
                    String subtitle = accessor.translate(sender, "game.title.prepare-to-fight-goodLuck.subtitle");
                    entity.getPlayer().sendTitle(title, subtitle, 5, 10, 5);
                    entity.getPlayer().sendMessage(accessor.translate(sender, "game.messages.fight-started"));
                });
                return;
            }
            this.players.forEach(entity -> {
                CrossCommandSender sender = CrossCommandSender.bukkit(entity.getPlayer());
                entity.getPlayer().sendMessage(accessor.translate(sender, "game.messages.prepare-to-fight").replace("%time", timer + ""));
            });
        } else if (this.state == GameState.WAITING) {
            switch (this.timer) {
                case 15, 10, 5, 4, 3, 2, 1 -> this.players.forEach(entity -> {
                    Player player = entity.getPlayer();
                    CrossCommandSender sender = CrossCommandSender.bukkit(player);
                    String title = accessor.translate(sender, "game.title.prepare-to-game.title").replace("%time", timer + "");
                    String subtitle = accessor.translate(sender, "game.title.prepare-to-game.subtitle").replace("%time", timer + "");
                    entity.getPlayer().sendTitle(title, subtitle, 5, 10, 5);
                });
            }
            if (this.timer == 0) {
                start();
                this.players.forEach(entity -> {
                    Player player = entity.getPlayer();
                    CrossCommandSender sender = CrossCommandSender.bukkit(player);
                    String title = accessor.translate(sender, "game.title.prepare-to-game-goodLuck.title");
                    String subtitle = accessor.translate(sender, "game.title.prepare-to-game-goodLuck.subtitle");
                    entity.getPlayer().sendTitle(title, subtitle, 5, 10, 5);
                    player.sendMessage(Component.text(accessor.translate(sender, "game.messages.game-started")));
                });
                return;
            }
            this.players.forEach(entity -> {
                CrossCommandSender sender = CrossCommandSender.bukkit(entity.getPlayer());
                entity.getPlayer().sendMessage(accessor.translate(sender, "game.messages.prepare-to-game").replace("%time", timer + ""));
            });
        }
    }

    private BukkitTask task;

    private void startTimer(int timer) {
        this.timer = timer + 1;
        System.out.println("таймер начался нахер " + this.timer);
        if (task != null) task.cancel();
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            this.timer--;
            onTimerChange();
            System.out.println("timer: " + this.timer);
            if (this.timer == 0) {
                task.cancel();
                if (this.state == GameState.STARTED) {
                    HandlerList.unregisterAll(plugin.getDamageListener());
                }
            }
        }, 20, 20);
    }

    private List<Location> availableLocations;

    private void spawnPlayers() {
        if (availableLocations == null || availableLocations.size() == 0) {
            availableLocations = new ArrayList<>(this.spawnLocations);
        }
        Bukkit.getScheduler().runTask(plugin, () -> this.players.forEach(entity -> entity.teleport(getRandomLocation())));
    }

    private Location getRandomLocation() {
        int index = ThreadLocalRandom.current().nextInt(0, availableLocations.size());
        Location location = availableLocations.get(index);
        availableLocations.remove(index);

        return location;
    }

}
