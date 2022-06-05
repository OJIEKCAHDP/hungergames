package dev.ojiekcahdp.hungergames.utils.region;

import dev.ojiekcahdp.hungergames.Main;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dreammine.lib.dreamlib.collections.FastSet;
import net.dreammine.lib.dreamlib.plugin.BukkitDreamLibPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.EnderChest;

import java.util.*;

@Getter
public class Cuboid {

    private final Location first, second;
    private final int x1, x2, z1, z2;

    public Cuboid(Location first, Location second) {
        this.first = first;
        this.second = second;
        this.x1 = Math.min(first.getBlockX(), second.getBlockX());
        this.z1 = Math.min(first.getBlockZ(), second.getBlockZ());
        this.x2 = Math.max(first.getBlockX(), second.getBlockX());
        this.z2 = Math.max(first.getBlockZ(), second.getBlockZ());
    }

    @SneakyThrows
    public Set<Chest> findChests() {
        Set<BlockState> results = new HashSet<>();

        Set<Chunk> chunks = getChunks();

        for (Chunk chunk : chunks) {
            System.out.println("Chunk: " + chunk);
            chunk.load(true);
            for (BlockState tileEntity : chunk.getTileEntities()) {
                if (tileEntity instanceof Chest chest) {
                    results.add(chest);
                }
            }
        }

        System.out.println("results = " + results);
        return results.stream().map(blockState -> (Chest) blockState).collect(FastSet.toObjectSet());
    }

    @SneakyThrows
    public Set<EnderChest> findEnderChests() {
        Set<BlockState> results = new HashSet<>();

        Set<Chunk> chunks = getChunks();

        for (Chunk chunk : chunks) {
            System.out.println("Chunk: " + chunk);
            chunk.load(true);
            for (BlockState tileEntity : chunk.getTileEntities()) {
                if (tileEntity instanceof EnderChest chest) {
                    results.add(chest);
                }
            }
        }

        System.out.println("results = " + results);
        return results.stream().map(blockState -> (EnderChest) blockState).collect(FastSet.toObjectSet());
    }

    @SneakyThrows
    public Set<Chunk> getChunks() {
        Set<Chunk> result = new HashSet<>();
        World world = first.getWorld();

        //getChunksCoordinate().forEach((integer, integer2) -> result.add(world.getChunkAt(integer, integer2)));

        /*Chunk chunk = new Location(world, x1, 0, z1).getChunk();

        for (int x = chunk.getX(); x <= x2; x+=15) {
            for (int z = chunk.getZ(); z <= z2; z+=15) {
                result.add(new Location(world, x, 0, z).getChunk());
                x++;
                z++;
            }
        }*/

        for (int x = (x1 >> 4); x <= (x2 >> 4); x++) {
            for (int z = (z1 >> 4); z <= (z2 >> 4); z++) {
                Chunk chunk = world.getChunkAt(x, z);
                result.add(chunk);
            }
        }

        System.out.println("result = " + result);
        return result;
    }

    @SneakyThrows
    public HashMap<Integer, Integer> getChunksCoordinate() {
        HashMap<Integer, Integer> result = new HashMap<>();

        Chunk chunk = new Location(first.getWorld(), x1, 0, z1).getChunk();

        for (int x = chunk.getX(); x <= x2; x+=15) {
            for (int z = chunk.getZ(); z <= z2; z+=15) {
                result.put(x, z);
                x++;
                z++;
            }
        }

        System.out.println("result = " + result);
        return result;
    }

   /* @SneakyThrows
    public Set<Chest> findChests() {
        Set<BlockState> results = new HashSet<>();

        Set<Chunk> chunks = getChunks();

        for (Chunk chunk : chunks) {
            System.out.println("Chunk: " + chunk);
            first.getWorld().loadChunk(chunk);
            System.out.println(new HashSet<>(Arrays.asList(chunk.getTileEntities(false))));
            System.out.println(new HashSet<>(Arrays.asList(chunk.getTileEntities(true))));
            results.addAll(chunk.getTileEntities(tile -> tile instanceof Chest, true));
        }

        System.out.println("results = " + results);
        return results.stream().map(blockState -> (Chest) blockState).collect(FastSet.toObjectSet());
    }

    @SneakyThrows
    public Set<Chunk> getChunks() {
        Set<Chunk> result = new HashSet<>();
        World world = first.getWorld();

        getChunksCoordinate().forEach((integer, integer2) -> result.add(world.getChunkAt(integer, integer2)));

        System.out.println("result = " + result);
        return result;
    }

    @SneakyThrows
    public HashMap<Integer, Integer> getChunksCoordinate() {
        HashMap<Integer, Integer> result = new HashMap<>();
        BukkitDreamLibPlugin.runBukkitThread(() -> {
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    result.put(x, z);
                }
            }
        }).get();
        System.out.println("result = " + result);
        return result;
    }*/

}
