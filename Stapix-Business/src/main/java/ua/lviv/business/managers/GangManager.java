package ua.lviv.business.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.lviv.business.LvivBusiness;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GangManager {

    private final LvivBusiness plugin;
    private final Map<String, Double> gangBalances = new HashMap<>();
    private final Map<String, String> gangLeaders = new HashMap<>(); // gang -> leader UUID
    private File gangFile;
    private FileConfiguration gangConfig;

    // Дозволені години для стрілок (наприклад: 18, 19, 20, 21)
    private List<Integer> allowedFightHours = new ArrayList<>();

    public GangManager(LvivBusiness plugin) {
        this.plugin = plugin;
        loadGangData();
        loadAllowedHours();
    }

    private void loadAllowedHours() {
        allowedFightHours = plugin.getConfig().getIntegerList("gang.allowed-fight-hours");
        if (allowedFightHours.isEmpty()) {
            allowedFightHours = Arrays.asList(18, 19, 20, 21);
        }
    }

    private void loadGangData() {
        gangFile = new File(plugin.getDataFolder(), "gangs.yml");
        if (!gangFile.exists()) {
            try { gangFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        gangConfig = YamlConfiguration.loadConfiguration(gangFile);

        if (gangConfig.getConfigurationSection("gangs") != null) {
            for (String gang : gangConfig.getConfigurationSection("gangs").getKeys(false)) {
                gangBalances.put(gang, gangConfig.getDouble("gangs." + gang + ".balance", 0));
                gangLeaders.put(gang, gangConfig.getString("gangs." + gang + ".leader", ""));
            }
        }
    }

    public void saveGangData() {
        for (Map.Entry<String, Double> entry : gangBalances.entrySet()) {
            gangConfig.set("gangs." + entry.getKey() + ".balance", entry.getValue());
            gangConfig.set("gangs." + entry.getKey() + ".leader", gangLeaders.getOrDefault(entry.getKey(), ""));
        }
        try { gangConfig.save(gangFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void addToGangBalance(String gangName, double amount) {
        gangBalances.put(gangName, gangBalances.getOrDefault(gangName, 0.0) + amount);
        saveGangData();
    }

    public double getGangBalance(String gangName) {
        return gangBalances.getOrDefault(gangName, 0.0);
    }

    public boolean isAllowedFightHour() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return allowedFightHours.contains(hour);
    }

    public List<Integer> getAllowedFightHours() { return allowedFightHours; }
    public void setAllowedFightHours(List<Integer> hours) {
        this.allowedFightHours = hours;
        plugin.getConfig().set("gang.allowed-fight-hours", hours);
        plugin.saveConfig();
    }

    public String getGangLeader(String gangName) {
        return gangLeaders.getOrDefault(gangName, "");
    }

    public void setGangLeader(String gangName, String playerUUID) {
        gangLeaders.put(gangName, playerUUID);
        saveGangData();
    }
}
