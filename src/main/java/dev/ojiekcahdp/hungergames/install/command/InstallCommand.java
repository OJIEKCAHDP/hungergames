package dev.ojiekcahdp.hungergames.install.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.ojiekcahdp.hungergames.Main;
import dev.ojiekcahdp.hungergames.utils.Utils;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dreammine.lib.dreamlib.language.LanguageAPI;
import net.dreammine.lib.dreamlib.platform.adapter.CrossCommandSender;
import net.dreammine.lib.dreamlib.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandAlias("hg")
public class InstallCommand extends BaseCommand {

    private final Main plugin;
    private @Getter
    ItemStack arenaItem;
    private @Getter
    ItemStack locationItem;
    private LanguageAPI.LanguageAccessor accessor;

    public InstallCommand(Main main, LanguageAPI.LanguageAccessor accessor) {
        this.plugin = main;
        this.accessor = accessor;
        setup();
    }

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.help")));

    }

    @Subcommand("setup")
    public void setup(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /hg setup <map>"));
            return;
        }

        plugin.getConfig().set("map", args[0]);

        player.getInventory().addItem(this.arenaItem);
        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.setup")));
    }

    @Subcommand("waitinglobby")
    public void waitingLobby(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        plugin.getConfig().set("waiting-lobby", Utils.getStringFromLocation(player.getLocation()));

        player.getInventory().addItem(this.locationItem);
        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.waitinglobby")));
    }

    HashMap<ItemStack, Integer> lootDefault = new HashMap<>();
    HashMap<ItemStack, Integer> lootEnder = new HashMap<>();

    @Subcommand("chest")
    public void chest(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 2 || !StringUtils.isNumeric(args[0]) || (!args[1].equalsIgnoreCase("ender") && !args[1].equalsIgnoreCase("default"))) {
            player.sendMessage(Component.text(accessor.translate(sender, "arena.command.waitingtime")));
            return;
        }

        int chance = Integer.parseInt(args[0]);
        String type = args[1].toLowerCase();
        int count;

        if (type.equalsIgnoreCase("default")) {
            lootDefault.put(player.getInventory().getItemInMainHand(), chance);
            count = lootDefault.size();
        } else {
            lootEnder.put(player.getInventory().getItemInMainHand(), chance);
            count = lootEnder.size();
        }

        System.out.println(player.getInventory().getItemInMainHand());

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.chest").replace("%count", count + "")));
    }

    @SneakyThrows
    @Subcommand("kit")
    public void kit(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text(accessor.translate(sender, "arena.command.kit.usage")));
            return;
        }

        String donate = args[0];
        String decode = Utils.serializeItems(player.getInventory().getContents());

        File file = new File(plugin.getDataFolder(), "/kits/" + donate + ".yml");
        if (file.exists()) file.createNewFile();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set("items", decode);
        yaml.set("cost", 100);
        yaml.set("item", "STONE");
        yaml.set("slot", 0);
        yaml.set("language-path.bought.lore", "game.kit." + donate + ".lore");
        yaml.set("language-path.bought.name", "game.kit." + donate + ".name");
        yaml.set("language-path.selected.lore", "game.kit." + donate + ".lore");
        yaml.set("language-path.selected.name", "game.kit." + donate + ".name");
        yaml.set("language-path.default.lore", "game.kit." + donate + ".lore");
        yaml.set("language-path.default.name", "game.kit." + donate + ".name");
        yaml.save(file);

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.kit")
                .replace("%donate", donate)));

    }

    @Subcommand("npc")
    public void npc(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        plugin.getConfig().set("npc-teleport-location", Utils.getStringFromLocation(player.getLocation()));

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.npc")));

    }

    @Subcommand("mob")
    public void mob(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /hg mob <type>"));
        }

        String type = args[0].toUpperCase();

        if (!EnumUtils.isValidEnum(EntityType.class, type)) {
            player.sendMessage(Component.text("Wrong mob type"));
            return;
        }

        int number = getMobsSize() + 1;

        plugin.getConfig().set("mobs." + number + ".type", type);
        plugin.getConfig().set("mobs." + number + ".location", Utils.getStringFromLocation(player.getLocation()));
        plugin.getConfig().set("mobs." + number + ".weapon", "null");
        plugin.getConfig().set("mobs." + number + ".armor", new ArrayList<>());

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.mob").replace("%type", type)));

    }

    @Subcommand("enderchest")
    public void enderchest(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /hg enderchest <time> - в секундах"));
        }

        if (!StringUtils.isNumeric(args[0])) {
            player.sendMessage(Component.text("Wrong time"));
            return;
        }

        int time = Integer.parseInt(args[0]);

        plugin.getConfig().set("ender-chest-open-time", time);

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.enderchest").replace("%time", time + "")));

    }

    @Subcommand("damagecooldown")
    public void damagecooldown(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /hg damagecooldown <time> - в секундах"));
        }

        if (!StringUtils.isNumeric(args[0])) {
            player.sendMessage(Component.text("Wrong time"));
            return;
        }

        int time = Integer.parseInt(args[0]);

        plugin.getConfig().set("damage-cooldown", time);

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.damagecooldown").replace("%time", time + "")));

    }

    @Subcommand("chestrefill")
    public void chestrefill(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /hg damagecooldown <time> - в секундах"));
        }

        if (!StringUtils.isNumeric(args[0])) {
            player.sendMessage(Component.text("Wrong time"));
            return;
        }

        int time = Integer.parseInt(args[0]);

        plugin.getConfig().set("chest-refill-time", time);

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.chestrefill").replace("%time", time + "")));

    }

    @Subcommand("waitingtime")
    public void waitingtime(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        String[] args = getOrigArgs();

        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /hg waitingtime <time> - в секундах"));
        }

        if (!StringUtils.isNumeric(args[0])) {
            player.sendMessage(Component.text("Wrong time"));
            return;
        }

        int time = Integer.parseInt(args[0]);

        plugin.getConfig().set("waiting-time", time);

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.waitingtime").replace("%time", time + "")));

    }

    @Subcommand("save")
    public void save(Player player) {
        CrossCommandSender sender = CrossCommandSender.bukkit(player);

        if (lootDefault.size() > 0)
        plugin.getLoot().set("chest.items", Utils.serializeLoot(lootDefault));
        if (lootEnder.size() > 0)
        plugin.getLoot().set("ender-chest.items", Utils.serializeLoot(lootEnder));

        plugin.saveLoot();
        plugin.saveConfig();

        player.sendMessage(Component.text(accessor.translate(sender, "arena.command.save")));

    }

    private int getMobsSize() {
        return plugin.getConfig().getConfigurationSection("mobs").getKeys(false).size();
    }

    private void setup() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.BLAZE_ROD);

        itemBuilder.setName("§aНастройка арены");
        itemBuilder.setLore(Stream.of(
                "Выберите первую и вторую ",
                "Точку, где начинается и ",
                "Заканчивается арена"
        ).collect(Collectors.toList()));

        this.arenaItem = itemBuilder.build();

        itemBuilder.setName("§aУстановка точек спавна");
        itemBuilder.setLore(Stream.of(
                "Нажимайте на блок, на котором ",
                "должен появиться игрок ",
                "",
                "Когда закончите, используйте команду",
                "/hg setup done"
        ).collect(Collectors.toList()));

        this.locationItem = itemBuilder.build();

    }

}
