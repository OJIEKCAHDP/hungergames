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

        languageAccessor.addTranslation("game.actionbar.enderchest", "Сможете открыть через: %time");
        languageAccessor.addTranslation("game.title.prepare-to-fight.title", "Готовьтесь к битве: ");
        languageAccessor.addTranslation("game.title.prepare-to-fight.subtitle", "%time");
        languageAccessor.addTranslation("game.title.prepare-to-fight-goodLuck.title", "Удачи!");
        languageAccessor.addTranslation("game.title.prepare-to-fight-goodLuck.subtitle", "");
        languageAccessor.addTranslation("game.title.prepare-to-game.title", "Игра начнётся: ");
        languageAccessor.addTranslation("game.title.prepare-to-game.subtitle", "%time");
        languageAccessor.addTranslation("game.title.prepare-to-game-goodLuck.title", "Удачи!");
        languageAccessor.addTranslation("game.title.prepare-to-game-goodLuck.subtitle", "");
        languageAccessor.addTranslation("game.messages.prepare-to-fight", "Вы сможете драться через %time");
        languageAccessor.addTranslation("game.messages.fight-started", "Удачи в битвах!");
        languageAccessor.addTranslation("game.messages.prepare-to-game", "Игра начнётся через %time");
        languageAccessor.addTranslation("game.messages.game-started", "Игра началась");
        languageAccessor.addTranslation("game.messages.kit.selected", "Вы выбрали кит: %kit");
        languageAccessor.addTranslation("game.messages.kit.bought", "Вы купили кит: %kit");
        languageAccessor.addTranslation("game.messages.kit.no-money", "У вас недостаточно средств");

        languageAccessor.addTranslation("arena.not.configured",
                "Арена не настроена, проблемная секция конфига: %reason\nИспользуйте /hg, чтобы прочитать руководство по настройке");
        languageAccessor.addTranslation("arena.command.help", "setup <map>");
        languageAccessor.addTranslation("arena.command.setup", "Выделите арену");
        languageAccessor.addTranslation("arena.command.waitinglobby", "Точка лобби ожидания установлена, используйте /hg waitingtime <time> - в секундах, чтобы установить время ожидания");
        languageAccessor.addTranslation("arena.command.waitingtime", "Время ожидания установлено, используйте /hg chest <chance> <type> -> (ender,default), чтобы установить лут, либо установите точки спавна игроков");
        languageAccessor.addTranslation("arena.command.chest", "Вы добавили предмет в сундуки, предметов в сундуках данного типа: %count\nесли вы закончили - используйте /hg kit <donate>");
        languageAccessor.addTranslation("arena.command.kit", "Успешно создали кит для доната: %donate, можете продолжить, либо установить точку спавна NPC - /hg npc");
        languageAccessor.addTranslation("arena.command.npc", "Вы установили точку телепорта, теперь можете установить точки спавна мобов, - /hg mob <type>");
        languageAccessor.addTranslation("arena.command.mob", "Вы добавили нового моба с типом %type, можете продолжать добавлять, либо установить время для открытия эндер-сундука - /hg enderchest <time> - в секундах");
        languageAccessor.addTranslation("arena.command.enderchest", "Вы установили начальную задержку на использование эндерсундука - %time секунд, осталось установить время до начала битвы - /hg damagecooldown <time> - в секундах");
        languageAccessor.addTranslation("arena.command.damagecooldown", "Вы установили время до начала битвы, конечная - период обновления сундуков - /hg chestrefill <time> - в секундах");
        languageAccessor.addTranslation("arena.command.chestrefill", "Отлично! Арена почти настроена, осталось сохранить всё - /hg save");
        languageAccessor.addTranslation("arena.command.save", "Арена сохранена и настроена. При следующем запуске сервера начнётся игра");

        languageAccessor.addTranslation("arena.select.first", "Первая точка установлена");
        languageAccessor.addTranslation("arena.select.second", "Вторая точка установлена, далее используйте /hg waitinglobby для установки точки лобби ожидания");

        languageAccessor.applyChanges();

    }

    @SneakyThrows
    public void saveLoot() {
        this.getLoot().save(new File(getDataFolder(), "loot.yml"));
    }

}
