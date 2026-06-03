package ua.lviv.business;

import org.bukkit.plugin.java.JavaPlugin;
import ua.lviv.business.managers.*;
import ua.lviv.business.commands.*;
import ua.lviv.business.listeners.*;

public class LvivBusiness extends JavaPlugin {

    private static LvivBusiness instance;
    private BusinessManager businessManager;
    private TaxManager taxManager;
    private GangManager gangManager;
    private HologramManager hologramManager;
    private ChatInputManager chatInputManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Ініціалізація менеджерів
        this.chatInputManager = new ChatInputManager(this);
        this.hologramManager = new HologramManager(this);
        this.businessManager = new BusinessManager(this);
        this.taxManager = new TaxManager(this);
        this.gangManager = new GangManager(this);

        // Реєстрація команд
        getCommand("business").setExecutor(new BusinessCommand(this));
        getCommand("businessadmin").setExecutor(new BusinessAdminCommand(this));
        getCommand("buybusiness").setExecutor(new BuyBusinessCommand(this));
        getCommand("sellbusiness").setExecutor(new SellBusinessCommand(this));
        getCommand("claimbusiness").setExecutor(new ClaimBusinessCommand(this));

        // Реєстрація подій
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCClickListener(this), this);

        // Запуск таймерів
        taxManager.startTaxTimer();
        businessManager.startSalaryTimer();

        getLogger().info("LvivBusiness завантажено!");
    }

    @Override
    public void onDisable() {
        businessManager.saveAll();
        getLogger().info("LvivBusiness вимкнено!");
    }

    public static LvivBusiness getInstance() { return instance; }
    public BusinessManager getBusinessManager() { return businessManager; }
    public TaxManager getTaxManager() { return taxManager; }
    public GangManager getGangManager() { return gangManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
}
