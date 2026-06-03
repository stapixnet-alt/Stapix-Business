package ua.lviv.business.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.models.Business;

import java.io.File;
import java.util.*;

public class NPCShopGUI implements Listener {

    private final LvivBusiness plugin;
    private final Business business;
    private static final Map<UUID, Business> openShops = new HashMap<>();

    public NPCShopGUI(LvivBusiness plugin, Business business) {
        this.plugin = plugin;
        this.business = business;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§6Магазин: §e" + business.getName());

        // Завантажити товари з конфігу
        File shopFile = new File(plugin.getDataFolder(), "shops/" + business.getId() + ".yml");
        if (shopFile.exists()) {
            FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
            if (shopConfig.getConfigurationSection("items") != null) {
                for (String key : shopConfig.getConfigurationSection("items").getKeys(false)) {
                    String path = "items." + key;
                    int slot = shopConfig.getInt(path + ".slot", 0);
                    String materialStr = shopConfig.getString(path + ".material", "STONE");
                    double price = shopConfig.getDouble(path + ".price", 100);
                    String displayName = shopConfig.getString(path + ".name", materialStr);
                    List<String> lore = shopConfig.getStringList(path + ".lore");

                    try {
                        Material material = Material.valueOf(materialStr);
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("§e" + displayName);
                        List<String> fullLore = new ArrayList<>(lore);
                        fullLore.add("");
                        fullLore.add("§7Ціна: §a" + String.format("%.0f", price) + " ₴");
                        fullLore.add("§aНатисніть щоб купити");
                        meta.setLore(fullLore);
                        item.setItemMeta(meta);
                        if (slot < 36) inv.setItem(slot, item);
                    } catch (Exception ignored) {}
                }
            }
        } else {
            player.sendMessage("§cМагазин ще не налаштований адміном.");
        }

        openShops.put(player.getUniqueId(), business);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().startsWith("§6Магазин:")) return;
        event.setCancelled(true);

        Business business = openShops.get(player.getUniqueId());
        if (business == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Знайти ціну товару
        File shopFile = new File(plugin.getDataFolder(), "shops/" + business.getId() + ".yml");
        if (!shopFile.exists()) return;

        FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        int slot = event.getSlot();

        if (shopConfig.getConfigurationSection("items") != null) {
            for (String key : shopConfig.getConfigurationSection("items").getKeys(false)) {
                String path = "items." + key;
                if (shopConfig.getInt(path + ".slot") == slot) {
                    double price = shopConfig.getDouble(path + ".price", 100);
                    String materialStr = shopConfig.getString(path + ".material", "STONE");

                    if (plugin.getBusinessManager().getEconomy().getBalance(player) < price) {
                        player.sendMessage("§cНедостатньо коштів! Потрібно: §e" +
                            String.format("%.0f", price) + " ₴");
                        return;
                    }

                    try {
                        Material material = Material.valueOf(materialStr);
                        plugin.getBusinessManager().getEconomy().withdrawPlayer(player, price);
                        player.getInventory().addItem(new ItemStack(material));

                        // Додати дохід власнику бізнесу
                        if (business.hasOwner()) {
                            plugin.getBusinessManager().addSaleRevenue(business, price);
                        }

                        player.sendMessage("§a✔ Куплено §e" + clicked.getItemMeta().getDisplayName() +
                            " §aза §e" + String.format("%.0f", price) + " ₴");
                    } catch (Exception e) {
                        player.sendMessage("§cПомилка при покупці!");
                    }
                    return;
                }
            }
        }
    }
}
