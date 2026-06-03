package ua.lviv.stapixbusiness.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.models.Business;

import java.io.File;
import java.util.*;

public class NPCShopGUI implements Listener {

    private static NPCShopGUI instance;
    private final StapixBusiness plugin;
    private static final Map<UUID, Business> openShops = new HashMap<>();

    public NPCShopGUI(StapixBusiness plugin, Business business) {
        this.plugin = plugin;
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Business business = openShops.get(player.getUniqueId());
        if (business == null) return;
        openWithBusiness(player, business);
    }

    public void open(Player player, Business business) {
        openShops.put(player.getUniqueId(), business);
        openWithBusiness(player, business);
    }

    private void openWithBusiness(Player player, Business business) {
        Inventory inv = Bukkit.createInventory(null, 36, "§6Магазин: §e" + business.getName());

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

                    try {
                        Material material = Material.valueOf(materialStr);
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("§e" + displayName);
                        meta.setLore(Arrays.asList(
                            "§7Ціна за 1 шт: §a" + String.format("%.0f", price) + " ₴",
                            "§7Ціна за 5 шт: §a" + String.format("%.0f", price * 5) + " ₴",
                            "",
                            "§aЛКМ §7— купити §e1 шт",
                            "§aПКМ §7— купити §e5 шт"
                        ));
                        item.setItemMeta(meta);
                        if (slot < 36) inv.setItem(slot, item);
                    } catch (Exception ignored) {}
                }
            }
        } else {
            player.sendMessage("§cМагазин ще не налаштований адміном.");
        }

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

        // Визначити кількість — ЛКМ = 1, ПКМ = 5
        int amount = event.getClick() == ClickType.RIGHT ? 5 : 1;

        File shopFile = new File(plugin.getDataFolder(), "shops/" + business.getId() + ".yml");
        if (!shopFile.exists()) return;

        FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        int slot = event.getSlot();

        if (shopConfig.getConfigurationSection("items") != null) {
            for (String key : shopConfig.getConfigurationSection("items").getKeys(false)) {
                String path = "items." + key;
                if (shopConfig.getInt(path + ".slot") == slot) {
                    double pricePerItem = shopConfig.getDouble(path + ".price", 100);
                    double totalPrice = pricePerItem * amount;
                    String materialStr = shopConfig.getString(path + ".material", "STONE");
                    String displayName = shopConfig.getString(path + ".name", materialStr);

                    if (plugin.getBusinessManager().getEconomy().getBalance(player) < totalPrice) {
                        player.sendMessage("§cНедостатньо коштів! Потрібно: §e" +
                            String.format("%.0f", totalPrice) + " ₴");
                        return;
                    }

                    try {
                        Material material = Material.valueOf(materialStr);
                        plugin.getBusinessManager().getEconomy().withdrawPlayer(player, totalPrice);

                        // Додати товар в інвентар
                        ItemStack buyItem = new ItemStack(material, amount);
                        player.getInventory().addItem(buyItem);

                        // Дохід власнику
                        if (business.hasOwner()) {
                            plugin.getBusinessManager().addSaleRevenue(business, totalPrice);
                        }

                        player.sendMessage("§a✔ Куплено §e" + amount + "x " + displayName +
                            " §aза §e" + String.format("%.0f", totalPrice) + " ₴");
                    } catch (Exception e) {
                        player.sendMessage("§cПомилка при покупці!");
                    }
                    return;
                }
            }
        }
    }
}
