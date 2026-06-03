package ua.lviv.stapixbusiness;

import org.bukkit.plugin.java.JavaPlugin;
import ua.lviv.stapixbusiness.managers.*;
import ua.lviv.stapixbusiness.commands.*;
import ua.lviv.stapixbusiness.listeners.*;

public class StapixBusiness extends JavaPlugin {

    private static StapixBusiness instance;
    private BusinessManager businessManager;
    private TaxManager taxManager;
    private GangManager gangManager;
    private HologramManager hologramManager;
    private ChatInputManager chatInputManager;
    private BuyBusinessCommand buyBusinessCommand;

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
        buyBusinessCommand = new BuyBusinessCommand(this);
        getCommand("bs").setExecutor(new BusinessCommand(this));
        getCommand("businessadmin").setExecutor(new BusinessAdminCommand(this));
        getCommand("buybusiness").setExecutor(buyBusinessCommand);
        getCommand("sellbusiness").setExecutor(new SellBusinessCommand(this));
        getCommand("claimbusiness").setExecutor(new ClaimBusinessCommand(this));
        getCommand("yes").setExecutor(new ConfirmCommand(this, true));
        getCommand("no").setExecutor(new ConfirmCommand(this, false));

        // Реєстрація подій
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCClickListener(this), this);

        // Запуск таймерів
        taxManager.startTaxTimer();
        businessManager.startSalaryTimer();

        getLogger().info("StapixBusiness завантажено!");
    }

    @Override
    public void onDisable() {
        businessManager.saveAll();
        getLogger().info("StapixBusiness вимкнено!");
    }

    public static StapixBusiness getInstance() { return instance; }
    public BusinessManager getBusinessManager() { return businessManager; }
    public TaxManager getTaxManager() { return taxManager; }
    public GangManager getGangManager() { return gangManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public BuyBusinessCommand getBuyBusinessCommand() { return buyBusinessCommand; }
}
