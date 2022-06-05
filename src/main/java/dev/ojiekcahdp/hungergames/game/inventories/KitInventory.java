package dev.ojiekcahdp.hungergames.game.inventories;

import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.game.entity.GameEntity;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dreammine.lib.dreamlib.language.LanguageAPI;
import net.dreammine.lib.dreamlib.manager.player.EconomyManager;
import net.dreammine.lib.dreamlib.platform.adapter.CrossCommandSender;
import net.dreammine.lib.dreamlib.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.stonlex.bukkit.inventory.impl.BaseSimpleInventory;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class KitInventory extends BaseSimpleInventory {

    private final Main plugin = Main.getInstance();
    private final LanguageAPI.LanguageAccessor accessor;

    public KitInventory(Player player, LanguageAPI.LanguageAccessor accessor) {
        super(8, accessor.translate(CrossCommandSender.bukkit(player), "arena.kit.inventory.name"));
        this.accessor = accessor;
    }

    @SneakyThrows
    @Override
    public void drawInventory(@NonNull Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        GameEntity entity = GameEntity.fromPlayer(player);

        Files.walk(new File(plugin.getDataFolder(), "/kits").toPath(), FileVisitOption.FOLLOW_LINKS).forEach(path -> {
            File file = new File(path.toString());
            if (file.getName().endsWith(".yml")) {
                String kitName = file.getName().replace(".yml", "");

                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

                int cost = yaml.getInt("cost");
                Material material = Material.valueOf(yaml.getString("items").toUpperCase());
                List<String> lore;
                String name;

                if (entity.getSelectedKit().equalsIgnoreCase(kitName)) {
                    lore = Arrays.stream(accessor.translate(sender, yaml.getString("language-path.selected.lore"))
                            .replace("%cost", cost + "")
                            .split("\n"))
                            .toList();
                    name = accessor.translate(sender, yaml.getString("language-path.selected.name"));
                } else if (entity.getAvailableKits().contains(kitName)) {
                    lore = Arrays.stream(accessor.translate(sender, yaml.getString("language-path.bought.lore"))
                            .replace("%cost", cost + "")
                            .split("\n"))
                            .toList();
                    name = accessor.translate(sender, yaml.getString("language-path.bought.name"));
                } else {
                    lore = Arrays.stream(accessor.translate(sender, yaml.getString("language-path.default.lore"))
                            .replace("%cost", cost + "")
                            .split("\n"))
                            .toList();
                    name = accessor.translate(sender, yaml.getString("language-path.default.name"));
                }


                addItem(yaml.getInt("slot"), new ItemBuilder(material)
                        .setName(name)
                        .setLore(lore)
                        .build(), (player1, e) -> {

                    if (entity.getAvailableKits().contains(kitName)) {

                        entity.setSelectedKit(kitName);
                        player.sendMessage(Component.text(accessor.translate(sender, "game.messages.kit.selected")
                                .replace("%kit", name)));

                        updateInventory(player);

                    } else {

                        if (EconomyManager.getBalance(player.getUniqueId()) >= cost) {

                            EconomyManager.setBalance(player.getUniqueId(), EconomyManager.getBalance(player.getUniqueId()) - cost);
                            entity.getAvailableKits().add(kitName);

                            player.sendMessage(Component.text(accessor.translate(sender, "game.messages.kit.bought")
                                    .replace("%kit", name)));

                            updateInventory(player);

                        } else {
                            player.sendMessage(Component.text(accessor.translate(sender, "game.messages.kit.no-money")
                                    .replace("%kit", name)));
                        }

                    }

                });

            }
        });
    }
}
