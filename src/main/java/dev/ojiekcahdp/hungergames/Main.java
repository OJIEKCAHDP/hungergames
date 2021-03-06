package dev.ojiekcahdp.hungergames;

import dev.ojiekcahdp.hungergames.game.arena.GameArena;
import dev.ojiekcahdp.hungergames.game.listeners.*;
import dev.ojiekcahdp.hungergames.install.command.InstallCommand;
import dev.ojiekcahdp.hungergames.install.listener.InstallListener;
import dev.ojiekcahdp.hungergames.utils.Utils;
import dev.ojiekcahdp.hungergames.utils.enums.ArenaSetupReason;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dreammine.lib.dreamlib.command.CommandAPI;
import net.dreammine.lib.dreamlib.language.LanguageAPI;
import net.dreammine.lib.dreamlib.manager.player.EconomyManager;
import net.dreammine.lib.dreamlib.platform.adapter.CrossPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.stonlex.bukkit.inventory.BaseInventoryManager;

import java.io.File;

@Getter
public class Main extends JavaPlugin {

    public static @Getter
    Main instance;

    public static BaseInventoryManager INVENTORY_MANAGER = new BaseInventoryManager();

    private GameArena arena;
    private DamageListener damageListener;
    private EnderChestListener enderChestListener;
    private FreezeListener freezeListener;
    private InstallCommand installCommand;
    private YamlConfiguration loot;
    private ArenaSetupReason setupReason;
    private LanguageAPI.LanguageAccessor languageAccessor;

    public void onEnable() {

        Main.instance = this;
        saveDefaultConfig();
        saveResource("loot.yml", false);
        loadLanguage();

        FileConfiguration config = getConfig();

        setupReason = Utils.arenaSetup(config);

        loot = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "loot.yml"));

        if (setupReason == ArenaSetupReason.NONE) {

            arena = new GameArena(config.getString("map"), config.getInt("waiting-time"), config.getInt("damage-cooldown"));
            damageListener = new DamageListener();
            freezeListener = new FreezeListener();
            enderChestListener = new EnderChestListener();
            registerEvents();

            arena.setAccessor(this.getLanguageAccessor());

        } else {
            installCommand = new InstallCommand(this, getLanguageAccessor());
            CommandAPI.registerCommand(installCommand);
            Bukkit.getPluginManager().registerEvents(new InstallListener(this, getLanguageAccessor()), this);
            //todo

        }

        if (new File(getDataFolder() + "/kits").mkdirs()) {
            getLogger().info("Created kits folder");
        }

    }

    private void registerEvents() {
        PluginManager manager = Bukkit.getPluginManager();

        manager.registerEvents(this.damageListener, this);
        manager.registerEvents(new GlobalListener(), this);
        manager.registerEvents(new NPCListener(), this);
    }

    private void loadLanguage() {
        languageAccessor = LanguageAPI.load(CrossPlugin.bukkit(this));

        languageAccessor.addTranslation("game.actionbar.enderchest", "?????????????? ?????????????? ??????????: %time");
        languageAccessor.addTranslation("game.title.prepare-to-fight.title", "???????????????????? ?? ??????????: ");
        languageAccessor.addTranslation("game.title.prepare-to-fight.subtitle", "%time");
        languageAccessor.addTranslation("game.title.prepare-to-fight-goodLuck.title", "??????????!");
        languageAccessor.addTranslation("game.title.prepare-to-fight-goodLuck.subtitle", "");
        languageAccessor.addTranslation("game.title.prepare-to-game.title", "???????? ????????????????: ");
        languageAccessor.addTranslation("game.title.prepare-to-game.subtitle", "%time");
        languageAccessor.addTranslation("game.title.prepare-to-game-goodLuck.title", "??????????!");
        languageAccessor.addTranslation("game.title.prepare-to-game-goodLuck.subtitle", "");
        languageAccessor.addTranslation("game.messages.prepare-to-fight", "???? ?????????????? ?????????????? ?????????? %time");
        languageAccessor.addTranslation("game.messages.fight-started", "?????????? ?? ????????????!");
        languageAccessor.addTranslation("game.messages.prepare-to-game", "???????? ???????????????? ?????????? %time");
        languageAccessor.addTranslation("game.messages.game-started", "???????? ????????????????");
        languageAccessor.addTranslation("game.messages.kit.selected", "???? ?????????????? ??????: %kit");
        languageAccessor.addTranslation("game.messages.kit.bought", "???? ???????????? ??????: %kit");
        languageAccessor.addTranslation("game.messages.kit.no-money", "?? ?????? ???????????????????????? ??????????????");

        languageAccessor.addTranslation("arena.not.configured",
                "?????????? ???? ??????????????????, ???????????????????? ???????????? ??????????????: %reason\n?????????????????????? /hg, ?????????? ?????????????????? ?????????????????????? ???? ??????????????????");
        languageAccessor.addTranslation("arena.command.help", "setup <map>");
        languageAccessor.addTranslation("arena.command.setup", "???????????????? ??????????");
        languageAccessor.addTranslation("arena.command.waitinglobby", "?????????? ?????????? ???????????????? ??????????????????????, ?????????????????????? /hg waitingtime <time> - ?? ????????????????, ?????????? ???????????????????? ?????????? ????????????????");
        languageAccessor.addTranslation("arena.command.waitingtime", "?????????? ???????????????? ??????????????????????, ?????????????????????? /hg chest <chance> <type> -> (ender,default), ?????????? ???????????????????? ??????, ???????? ???????????????????? ?????????? ???????????? ??????????????");
        languageAccessor.addTranslation("arena.command.chest", "???? ???????????????? ?????????????? ?? ??????????????, ?????????????????? ?? ???????????????? ?????????????? ????????: %count\n???????? ???? ?????????????????? - ?????????????????????? /hg kit <donate>");
        languageAccessor.addTranslation("arena.command.kit", "?????????????? ?????????????? ?????? ?????? ????????????: %donate, ???????????? ????????????????????, ???????? ???????????????????? ?????????? ???????????? NPC - /hg npc");
        languageAccessor.addTranslation("arena.command.npc", "???? ???????????????????? ?????????? ??????????????????, ???????????? ???????????? ???????????????????? ?????????? ???????????? ??????????, - /hg mob <type>");
        languageAccessor.addTranslation("arena.command.mob", "???? ???????????????? ???????????? ???????? ?? ?????????? %type, ???????????? ???????????????????? ??????????????????, ???????? ???????????????????? ?????????? ?????? ???????????????? ??????????-?????????????? - /hg enderchest <time> - ?? ????????????????");
        languageAccessor.addTranslation("arena.command.enderchest", "???? ???????????????????? ?????????????????? ???????????????? ???? ?????????????????????????? ???????????????????????? - %time ????????????, ???????????????? ???????????????????? ?????????? ???? ???????????? ?????????? - /hg damagecooldown <time> - ?? ????????????????");
        languageAccessor.addTranslation("arena.command.damagecooldown", "???? ???????????????????? ?????????? ???? ???????????? ??????????, ???????????????? - ???????????? ???????????????????? ???????????????? - /hg chestrefill <time> - ?? ????????????????");
        languageAccessor.addTranslation("arena.command.chestrefill", "??????????????! ?????????? ?????????? ??????????????????, ???????????????? ?????????????????? ?????? - /hg save");
        languageAccessor.addTranslation("arena.command.save", "?????????? ?????????????????? ?? ??????????????????. ?????? ?????????????????? ?????????????? ?????????????? ???????????????? ????????");

        languageAccessor.addTranslation("arena.select.first", "???????????? ?????????? ??????????????????????");
        languageAccessor.addTranslation("arena.select.second", "???????????? ?????????? ??????????????????????, ?????????? ?????????????????????? /hg waitinglobby ?????? ?????????????????? ?????????? ?????????? ????????????????");

        languageAccessor.applyChanges();

    }

    @SneakyThrows
    public void saveLoot() {
        this.getLoot().save(new File(getDataFolder(), "loot.yml"));
    }

}
