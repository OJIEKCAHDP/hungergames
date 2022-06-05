package dev.ojiekcahdp.hungergames.utils;

import dev.ojiekcahdp.hungergames.game.Arena;
import dev.ojiekcahdp.hungergames.utils.enums.ArenaSetupReason;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static Location getLocationFromString(String string) {

        if (string == null) return null;

        String[] args = string.split(";");

        Location location = new Location(
                Bukkit.getWorld(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2]),
                Double.parseDouble(args[3]));

        if (args.length == 6) {
            location.setYaw(Float.parseFloat(args[4]));
            location.setPitch(Float.parseFloat(args[5]));
        }

        return location;
    }

    public static String getStringFromLocation(Location location) {
        return location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }

    public static List<Location> getLocationListFromStringList(List<String> list) {
        List<Location> locations = new ArrayList<>();
        list.forEach(s -> locations.add(getLocationFromString(s)));
        return locations;
    }

    public static String getTimeString(int time) {
        int minutes = time / 60;
        int seconds = time - minutes * 60;
        String m = String.valueOf(minutes);
        String s = String.valueOf(seconds);
        if (m.length() == 1) {
            m = "0" + m;
        }

        if (s.length() == 1) {
            s = "0" + s;
        }

        return m + ":" + s;
    }

    public static List<Chest> getChestList(List<String> list) {
        List<Chest> chests = new ArrayList<>();

        list.forEach(s -> {
            Block block = Utils.getLocationFromString(s).getBlock();
            if (block.getType() == Material.CHEST) {
                chests.add((Chest) block.getState());
            }
        });

        return chests;
    }

    @SneakyThrows
    public static List<ItemStack> deserializeItems(String string) {
        List<ItemStack> items = new ArrayList<>();

        if (string.isEmpty()) return items;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        try {
            while (true) {
                items.add((ItemStack) dataInput.readObject());
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }


        dataInput.close();

        return items;
    }

    @SneakyThrows
    public static String serializeItems(ItemStack... items) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        for (ItemStack item : items)
            dataOutput.writeObject(item);
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @SneakyThrows
    public static String serializeLoot(HashMap<ItemStack, Integer> loot) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        loot.forEach((itemStack, integer) -> {
            try {
                dataOutput.writeObject(itemStack);
                dataOutput.writeInt(integer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @SneakyThrows
    public static HashMap<ItemStack, Integer> getLoot(YamlConfiguration configuration, String path) {
        String items = configuration.getString(path);
        HashMap<ItemStack, Integer> itemList = new HashMap<>();
        if (items.isEmpty()) return itemList;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(items));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        try {
            while (true) {
                itemList.put((ItemStack) dataInput.readObject(), dataInput.readInt());
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }


        dataInput.close();
        return itemList;
    }

    public static ArenaSetupReason arenaSetup(FileConfiguration config) {

        ArenaSetupReason setup = ArenaSetupReason.NONE;

        if (Objects.equals(config.getString("arena.first-position"), "none")) {
            setup = ArenaSetupReason.ARENA_FIRST_POSITION;
        } else if (Objects.equals(config.getString("arena.second-position"), "none")) {
            setup = ArenaSetupReason.ARENA_SECOND_POSITION;
        } else if (Objects.equals(config.getString("map"), "none")) {
            setup = ArenaSetupReason.MAP;
        } else if (Objects.equals(config.getString("waiting-lobby"), "none")) {
            setup = ArenaSetupReason.WAITING_LOBBY;
        } else if (Objects.equals(config.getString("waiting-time"), "none")) {
            setup = ArenaSetupReason.WAITING_TIME;
        } else if (Objects.equals(config.getString("chest-refill-time"), "none")) {
            setup = ArenaSetupReason.CHEST_REFILL_TIME;
        } else if (Objects.equals(config.getString("damage-cooldown"), "none")) {
            setup = ArenaSetupReason.DAMAGE_COOLDOWN;
        } else if (Objects.equals(config.getString("spawn-locations"), "none")) {
            setup = ArenaSetupReason.SPAWN_LOCATIONS;
        } else if (Objects.equals(config.getString("chests"), "none")) {
            setup = ArenaSetupReason.CHESTS;
        } else if (Objects.equals(config.getString("ender-chests"), "none")) {
            setup = ArenaSetupReason.ENDER_CHESTS;
        } else if (Objects.equals(config.getString("npc-teleport-location"), "none")) {
            setup = ArenaSetupReason.NPC_TELEPORT_LOCATION;
        } else if (Objects.equals(config.getString("ender-chest-open-time"), "none")) {
            setup = ArenaSetupReason.ENDER_CHEST_OPEN_TIME;
        }

        return setup;
    }

}
