package ua.lviv.stapixbusiness.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.models.Business;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BusinessManager {

    private final StapixBusiness plugin;
    private final Map<String, Business> businesses = new HashMap<>();
    private Economy economy;
    private File dataFile;
    private FileConfiguration dataConfig;

    public BusinessManager(StapixBusiness plugin) {
        this.plugin = plugin;
        setupEconomy();
        loadBusinesses();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public void loadBusinesses() {
        dataFile = new File(plugin.getDataFolder(), "businesses.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("businesses") == null) return;

        for (String id : dataConfig.getConfigurationSection("businesses").getKeys(false)) {
            String path = "businesses." + id;
            Business b = new Business(id,
                dataConfig.getString(path + ".name"),
                dataConfig.getDouble(path + ".buyPrice"));

            b.setHourlyWage(dataConfig.getDouble(path + ".hourlyWage", 100));
            b.setSalesPercent(dataConfig.getDouble(path + ".salesPercent", 10));
            b.setDailyTax(dataConfig.getDouble(path + ".dailyTax", 500));
            b.setMaxTaxDebt(dataConfig.getDouble(path + ".maxTaxDebt", 10000));
            b.setGangPercent(dataConfig.getDouble(path + ".gangPercent", 20));
            b.setCommissionPercent(dataConfig.getDouble(path + ".commissionPercent", 5));
            b.setBusinessBalance(dataConfig.getDouble(path + ".businessBalance", 0));
            b.setTaxDebt(dataConfig.getDouble(path + ".taxDebt", 0));
            b.setTaxDebtReachedMaxTime(dataConfig.getLong(path + ".taxDebtReachedMaxTime", 0));

            String ownerStr = dataConfig.getString(path + ".ownerUUID");
            if (ownerStr != null && !ownerStr.isEmpty()) {
                b.setOwnerUUID(UUID.fromString(ownerStr));
                b.setOwnerName(dataConfig.getString(path + ".ownerName"));
            }

            b.setGangOwner(dataConfig.getString(path + ".gangOwner", ""));
            b.setLastStreetFightTime(dataConfig.getLong(path + ".lastStreetFightTime", 0));
            b.setStreetFightScheduledTime(dataConfig.getLong(path + ".streetFightScheduledTime", 0));

            b.setNpcId(dataConfig.getInt(path + ".npcId", -1));
            b.setNpcWorld(dataConfig.getString(path + ".npcWorld", "world"));
            b.setNpcX(dataConfig.getDouble(path + ".npcX", 0));
            b.setNpcY(dataConfig.getDouble(path + ".npcY", 0));
            b.setNpcZ(dataConfig.getDouble(path + ".npcZ", 0));

            b.setHoloWorld(dataConfig.getString(path + ".holoWorld", "world"));
            b.setHoloX(dataConfig.getDouble(path + ".holoX", 0));
            b.setHoloY(dataConfig.getDouble(path + ".holoY", 0));
            b.setHoloZ(dataConfig.getDouble(path + ".holoZ", 0));

            businesses.put(id, b);
        }
    }

    public void saveAll() {
        for (Business b : businesses.values()) {
            saveBusiness(b);
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveBusiness(Business b) {
        String path = "businesses." + b.getId();
        dataConfig.set(path + ".name", b.getName());
        dataConfig.set(path + ".buyPrice", b.getBuyPrice());
        dataConfig.set(path + ".hourlyWage", b.getHourlyWage());
        dataConfig.set(path + ".salesPercent", b.getSalesPercent());
        dataConfig.set(path + ".dailyTax", b.getDailyTax());
        dataConfig.set(path + ".maxTaxDebt", b.getMaxTaxDebt());
        dataConfig.set(path + ".gangPercent", b.getGangPercent());
        dataConfig.set(path + ".commissionPercent", b.getCommissionPercent());
        dataConfig.set(path + ".businessBalance", b.getBusinessBalance());
        dataConfig.set(path + ".taxDebt", b.getTaxDebt());
        dataConfig.set(path + ".taxDebtReachedMaxTime", b.getTaxDebtReachedMaxTime());
        dataConfig.set(path + ".ownerUUID", b.getOwnerUUID() != null ? b.getOwnerUUID().toString() : "");
        dataConfig.set(path + ".ownerName", b.getOwnerName() != null ? b.getOwnerName() : "");
        dataConfig.set(path + ".gangOwner", b.getGangOwner() != null ? b.getGangOwner() : "");
        dataConfig.set(path + ".lastStreetFightTime", b.getLastStreetFightTime());
        dataConfig.set(path + ".streetFightScheduledTime", b.getStreetFightScheduledTime());
        dataConfig.set(path + ".npcId", b.getNpcId());
        dataConfig.set(path + ".npcWorld", b.getNpcWorld());
        dataConfig.set(path + ".npcX", b.getNpcX());
        dataConfig.set(path + ".npcY", b.getNpcY());
        dataConfig.set(path + ".npcZ", b.getNpcZ());
        dataConfig.set(path + ".holoWorld", b.getHoloWorld());
        dataConfig.set(path + ".holoX", b.getHoloX());
        dataConfig.set(path + ".holoY", b.getHoloY());
        dataConfig.set(path + ".holoZ", b.getHoloZ());
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public int getMaxBusinesses(Player player) {
        if (player.hasPermission("business.limit.oligarch")) return 3;
        if (player.hasPermission("business.limit.entrepreneur")) return 2;
        return 1;
    }

    public int countOwnedBusinesses(UUID playerUUID) {
        int count = 0;
        for (Business b : businesses.values()) {
            if (b.hasOwner() && b.getOwnerUUID().equals(playerUUID)) count++;
        }
        return count;
    }

    public void buyBusiness(Player player, Business business) {
        economy.withdrawPlayer(player, business.getBuyPrice());
        business.setOwnerUUID(player.getUniqueId());
        business.setOwnerName(player.getName());
        business.setBusinessBalance(0);
        business.setTaxDebt(0);
        business.setTaxDebtReachedMaxTime(0);
        saveBusiness(business);
        plugin.getHologramManager().updateHologram(business);
    }

    public void removeBusiness(Business business) {
        business.setOwnerUUID(null);
        business.setOwnerName(null);
        business.setBusinessBalance(0);
        business.setTaxDebt(0);
        business.setTaxDebtReachedMaxTime(0);
        saveBusiness(business);
        plugin.getHologramManager().updateHologram(business);
    }

    public void startSalaryTimer()
 {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Business b : businesses.values()) {
                if (!b.hasOwner()) continue;
                double salary = b.getHourlyWage();
                // Якщо є банда — відрахувати % для банди
                if (b.hasGang()) {
                    double gangCut = salary * (b.getGangPercent() / 100.0);
                    salary -= gangCut;
                    plugin.getGangManager().addToGangBalance(b.getGangOwner(), gangCut);
                }
                b.addToBalance(salary);
                saveBusiness(b);
            }
        }, 72000L, 72000L); // кожну годину (72000 тіків)
    }

    public void addSaleRevenue(Business business, double amount) {
        double ownerCut = amount * (business.getSalesPercent() / 100.0);
        if (business.hasGang()) {
            double gangCut = ownerCut * (business.getGangPercent() / 100.0);
            ownerCut -= gangCut;
            plugin.getGangManager().addToGangBalance(business.getGangOwner(), gangCut);
        }
        business.addToBalance(ownerCut);
        saveBusiness(business);
    }

    public Business getBusinessById(String id) { return businesses.get(id); }
    public Collection<Business> getAllBusinesses() { return businesses.values(); }
    public Economy getEconomy() { return economy; }

    public Business createBusiness(String id, String name, double price) {
        Business b = new Business(id, name, price);
        businesses.put(id, b);
        saveBusiness(b);
        return b;
    }

    public void deleteBusiness(String id) {
        Business b = businesses.get(id);
        if (b == null) return;
        businesses.remove(id);
        dataConfig.set("businesses." + id, null);
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }


}
