package dev.ojiekcahdp.hungergames.game.arena;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.game.Arena;
import dev.ojiekcahdp.hungergames.game.entity.GameEntity;
import dev.ojiekcahdp.hungergames.game.enums.GameState;
import dev.ojiekcahdp.hungergames.utils.Utils;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.stonlex.bukkit.scoreboard.BaseScoreboardBuilder;
import ru.stonlex.bukkit.scoreboard.BaseScoreboardScope;
import ru.stonlex.bukkit.scoreboard.animation.ScoreboardDisplayFlickAnimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameArena extends Arena {

    private final Main plugin = Main.getInstance();

    private @Getter
    final List<NPC> npcList = new ArrayList<>();

    public GameArena(String map, int waitingTime, int damageCooldown) {
        super(map, waitingTime, damageCooldown);
    }

    @Override
    public void onGameStart() {
        getPlayers().forEach(entity -> {
            Player player = entity.getPlayer();
            player.setHealth(20);
            setScoreboard(player);
            String donate = LuckPermsProvider.get().getUserManager().getUser(player.getName()).getPrimaryGroup();
            entity.giveKit(donate);
        });

        startRefill();
        spawnMobs(getWorld());

        Bukkit.getPluginManager().registerEvents(plugin.getEnderChestListener(), plugin);
        plugin.getEnderChestListener().startTimer();
    }

    @Override
    public void onGameEnded() {
        HandlerList.unregisterAll(plugin.getEnderChestListener());
        GameEntity entity = getAlivePlayers().get(0);
        entity.setWinner(true);
        entity.getPlayer().sendMessage(Component.text("Winner!"));

        Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 10 * 20);
    }

    @Override
    public void onPlayerJoin(Player player) {
        setScoreboard(player);
    }

    @Override
    public void onPlayerDeath(GameEntity gameEntity) {
        Player player = gameEntity.getPlayer();
        Player killer = player.getKiller();
        if (killer != null) GameEntity.fromPlayer(killer).incKill();
        gameEntity.death();
    }

    public void onStartWaitingTimer() {
        this.getPlayers().forEach(entity -> setScoreboard(entity.getPlayer()));
    }

    private void spawnMobs(World world) {
        FileConfiguration config = plugin.getConfig();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String section : config.getConfigurationSection("mobs").getKeys(false)) {

                String type = config.getString("mobs." + section + ".type");
                String weapon = config.getString("mobs." + section + ".weapon");
                List<String> armor = config.getStringList("mobs." + section + ".armor");

                Location location = Utils.getLocationFromString(config.getString("mobs." + section + ".location"));

                assert location != null;
                world.spawnEntity(location, EntityType.valueOf(type), CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        EntityEquipment equipment = livingEntity.getEquipment();
                        assert equipment != null;

                        if (EnumUtils.isValidEnum(Material.class, weapon)) equipment.setItemInMainHand(new ItemStack(Material.valueOf(weapon)));
                        for (int i = 0; i < armor.size(); i++) {
                            String armorType = armor.get(i).toUpperCase();
                            if (!EnumUtils.isValidEnum(Material.class, armorType)) continue;
                            ItemStack itemStack = new ItemStack(Material.valueOf(armorType));
                            switch (i) {
                                case 0 -> equipment.setHelmet(itemStack);
                                case 1 -> equipment.setChestplate(itemStack);
                                case 2 -> equipment.setLeggings(itemStack);
                                case 3 -> equipment.setBoots(itemStack);
                            }
                        }

                    }
                });

            }
        });
    }

    private void startRefill() {
        int refillTime = plugin.getConfig().getInt("chest-refill-time") * 20;
        HashMap<ItemStack, Integer> loot = Utils.getLoot(plugin.getLoot(), "chest.items");

        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            for (Chest chest : getChests()) {
                chest.getBlockInventory().clear();
                chest.update();
                setChestLoot(loot, chest.getBlockInventory(), chest);
            }
        }, 0, refillTime);
    }

    public HashMap<ItemStack, Integer> setChestLoot(HashMap<ItemStack, Integer> items, Inventory inventory, Chest chest) {
        HashMap<ItemStack, Integer> finalItems = new HashMap<>(items);
        items.forEach((itemStack, integer) -> {
            int random = ThreadLocalRandom.current().nextInt(0, 100);
            if (random <= integer) {
                int slot = getRandomEmptySlot(inventory);
                if (chest != null) chest.update();
                inventory.setItem(slot, itemStack);
                finalItems.put(itemStack, slot);
            }
        });

        return finalItems;
    }

    private int getRandomEmptySlot(Inventory inventory) {
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null)
                emptySlots.add(i);
        }
        return emptySlots.get(ThreadLocalRandom.current().nextInt(emptySlots.size()));
    }

    private void setScoreboard(Player player) {

        BaseScoreboardBuilder baseScoreboardBuilder = BaseScoreboardBuilder.newScoreboardBuilder();
        baseScoreboardBuilder.scoreboardScope(BaseScoreboardScope.PROTOTYPE);

        ScoreboardDisplayFlickAnimation scoreboardDisplayCustomAnimation = new ScoreboardDisplayFlickAnimation();

        scoreboardDisplayCustomAnimation.addColor(ChatColor.YELLOW);
        scoreboardDisplayCustomAnimation.addColor(ChatColor.GOLD);
        scoreboardDisplayCustomAnimation.addColor(ChatColor.WHITE);

        scoreboardDisplayCustomAnimation.addTextToAnimation("HungerGames");

        baseScoreboardBuilder.scoreboardDisplay(scoreboardDisplayCustomAnimation);

        switch (getState()) {
            case WAITING_FOR_PLAYERS -> {

                baseScoreboardBuilder.scoreboardLine(7, "");
                baseScoreboardBuilder.scoreboardLine(6, "§fКарта: §a" + getMap());
                baseScoreboardBuilder.scoreboardLine(5, "");
                baseScoreboardBuilder.scoreboardLine(4, "§fИгроков: §a" + getPlayers().size());
                baseScoreboardBuilder.scoreboardLine(3, "§fРейтинг: ");
                baseScoreboardBuilder.scoreboardLine(2, "§fВремени прошло: §a00:00");
                baseScoreboardBuilder.scoreboardLine(1, "");

                MutableInt timer = new MutableInt(0);

                baseScoreboardBuilder.scoreboardUpdater((baseScoreboard, player1) -> {
                    timer.increment();
                    baseScoreboard.updateScoreboardLine(4, player, "§fИгроков: §a" + getPlayers().size());
                    baseScoreboard.updateScoreboardLine(2, player, "§fВремени прошло: §a" + Utils.getTimeString(timer.getValue()));
                }, 20L);

            }
            case WAITING -> {

                baseScoreboardBuilder.scoreboardLine(7, "");
                baseScoreboardBuilder.scoreboardLine(6, "§fКарта: §a" + getMap());
                baseScoreboardBuilder.scoreboardLine(5, "");
                baseScoreboardBuilder.scoreboardLine(4, "§fИгроков: §a" + getPlayers().size());
                baseScoreboardBuilder.scoreboardLine(3, "§fРейтинг: ");
                baseScoreboardBuilder.scoreboardLine(2, "§fВремя до старта: §a00:00");
                baseScoreboardBuilder.scoreboardLine(1, "");

                baseScoreboardBuilder.scoreboardUpdater((baseScoreboard, player1) -> {
                    baseScoreboard.updateScoreboardLine(4, player, "§fИгроков: §a" + getPlayers().size());
                    baseScoreboard.updateScoreboardLine(2, player, "§fВремя до старта: §a" + Utils.getTimeString(getTimer()));
                }, 10L);

            }
            case STARTED -> {

                baseScoreboardBuilder.scoreboardLine(8, "");
                baseScoreboardBuilder.scoreboardLine(7, "§fКарта: §a" + getMap());
                baseScoreboardBuilder.scoreboardLine(6, "");
                baseScoreboardBuilder.scoreboardLine(5, "§aЖивых: §a" + getPlayers().size());
                baseScoreboardBuilder.scoreboardLine(4, "§fНаблюдателей: " + (getPlayers().size() - getAlivePlayers().size()));
                baseScoreboardBuilder.scoreboardLine(3, "§fРейтинг: ");
                baseScoreboardBuilder.scoreboardLine(2, "§fВремя до битвы: §a00:00");
                baseScoreboardBuilder.scoreboardLine(1, "");

                MutableInt timer = new MutableInt(0);

                baseScoreboardBuilder.scoreboardUpdater((baseScoreboard, player1) -> {
                    baseScoreboard.updateScoreboardLine(5, player, "§fЖивых: §a" + getPlayers().size());
                    baseScoreboard.updateScoreboardLine(4, player, "§fНаблюдателей: §a" + (getPlayers().size() - getAlivePlayers().size()));
                    if (getTimer() > 0)
                        baseScoreboard.updateScoreboardLine(2, player, "§fВремя до битвы: §a" + Utils.getTimeString(getTimer()));
                    else if (getState() != GameState.ENDED){
                        baseScoreboard.updateScoreboardLine(2, player, "§fВремя игры: §a" + Utils.getTimeString(timer.getValue()));
                        timer.increment();
                    }
                }, 20L);

            }

        }

        baseScoreboardBuilder.build().setScoreboardToPlayer(player);

    }

}
