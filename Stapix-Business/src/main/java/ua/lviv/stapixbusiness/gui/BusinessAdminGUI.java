package ua.lviv.stapixbusiness.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.models.Business;

import java.util.*;

public class BusinessAdminGUI implements Listener {

    private static BusinessAdminGUI instance;
    private final StapixBusiness plugin;
    private static final Map<UUID, String> selectedBusiness = new HashMap<>();

    public BusinessAdminGUI(StapixBusiness plugin) {
        this.plugin = plugin;
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        List<Business> businesses = new ArrayList<>(plugin.getBusinessManager().getAllBusinesses());
        int size = Math.max(9, Math.min(54, ((businesses.size() / 9) + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size, "§4Адмін: §cБізнеси");

        for (int i = 0; i < businesses.size() && i < size; i++) {
            Business b = businesses.get(i);
            String owner = b.hasOwner() ? "§e" + b.getOwnerName() : "§7Немає";
            ItemStack item = createItem(Material.CHEST, "§e" + b.getName(),
                "§7ID: §f" + b.getId(),
                "§7Власник: " + owner,
                "§7Ціна: §a" + String.format("%.0f", b.getBuyPrice()) + " ₴",
                "§7Зарплата: §a" + String.format("%.0f", b.getHourlyWage()) + " ₴/год",
                "§7Податок: §c" + String.format("%.0f", b.getDailyTax()) + " ₴/день",
                "",
                "§aНатисніть для налаштування"
            );
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    public void openBusinessSettings(Player player, Business business) {
        Inventory inv = Bukkit.createInventory(null, 36, "§4Налаштування: §c" + business.getName());

        inv.setItem(0, createItem(Material.GOLD_INGOT, "§eЗарплата",
            "§7Поточна: §a" + String.format("%.0f", business.getHourlyWage()) + " ₴/год",
            "§aНатисніть щоб змінити"));

        inv.setItem(1, createItem(Material.EMERALD, "§a% від продажів",
            "§7Поточний: §a" + business.getSalesPercent() + "%",
            "§aНатисніть щоб змінити"));

        inv.setItem(2, createItem(Material.PAPER, "§cПодаток",
            "§7Поточний: §c" + String.format("%.0f", business.getDailyTax()) + " ₴/день",
            "§aНатисніть щоб змінити"));

        inv.setItem(3, createItem(Material.BARRIER, "§cМакс. борг",
            "§7Поточний: §c" + String.format("%.0f", business.getMaxTaxDebt()) + " ₴",
            "§aНатисніть щоб змінити"));

        inv.setItem(4, createItem(Material.IRON_SWORD, "§c% банди",
            "§7Поточний: §c" + business.getGangPercent() + "%",
            "§aНатисніть щоб змінити"));

        inv.setItem(5, createItem(Material.GOLD_NUGGET, "§6Комісія",
            "§7Поточна: §6" + business.getCommissionPercent() + "%",
            "§aНатисніть щоб змінити"));

        inv.setItem(6, createItem(Material.DIAMOND, "§bЦіна покупки",
            "§7Поточна: §b" + String.format("%.0f", business.getBuyPrice()) + " ₴",
            "§aНатисніть щоб змінити"));

        // NPC прив'язка
        String npcInfo = business.getNpcId() >= 0 ? "§aID: " + business.getNpcId() : "§cНе прив'язаний";
        inv.setItem(8, createItem(Material.VILLAGER_SPAWN_EGG, "§eПрив'язати NPC",
            "§7Поточний NPC: " + npcInfo,
            "§7Введіть ID NPC після натискання",
            "§aНатисніть щоб прив'язати"));

        // Налаштування товарів
        inv.setItem(9, createItem(Material.CHEST, "§6Товари магазину",
            "§7Додати/видалити/змінити товари",
            "§aНатисніть щоб відкрити"));

        // Видалити бізнес
        inv.setItem(17, createItem(Material.TNT, "§4Видалити бізнес",
            "§cВидаляє бізнес, голограму і NPC",
            "§4Натисніть щоб видалити"));

        // Назад
        inv.setItem(27, createItem(Material.ARROW, "§7Назад", "§7Повернутись до списку"));

        selectedBusiness.put(player.getUniqueId(), business.getId());
        player.openInventory(inv);
    }

    public void openShopItems(Player player, Business business) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Товари: §e" + business.getName());

        // Завантажити існуючі товари
        java.io.File shopFile = new java.io.File(plugin.getDataFolder(), "shops/" + business.getId() + ".yml");
        if (shopFile.exists()) {
            org.bukkit.configuration.file.FileConfiguration shopConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile);
            if (shopConfig.getConfigurationSection("items") != null) {
                for (String key : shopConfig.getConfigurationSection("items").getKeys(false)) {
                    String path = "items." + key;
                    int slot = shopConfig.getInt(path + ".slot", 0);
                    String materialStr = shopConfig.getString(path + ".material", "STONE");
                    double price = shopConfig.getDouble(path + ".price", 100);
                    String displayName = shopConfig.getString(path + ".name", materialStr);
                    try {
                        Material mat = Material.valueOf(materialStr);
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName("§e" + displayName);
                        meta.setLore(Arrays.asList(
                            "§7Ціна: §a" + String.format("%.0f", price) + " ₴",
                            "",
                            "§cПКМ - видалити товар",
                            "§eЛКМ - змінити ціну"
                        ));
                        item.setItemMeta(meta);
                        if (slot < 45) inv.setItem(slot, item);
                    } catch (Exception ignored) {}
                }
            }
        }

        // Кнопка додати товар
        inv.setItem(45, createItem(Material.LIME_DYE, "§aДодати товар",
            "§7Натисніть щоб додати новий товар"));

        // Назад
        inv.setItem(53, createItem(Material.ARROW, "§7Назад", "§7Повернутись"));

        selectedBusiness.put(player.getUniqueId(), business.getId());
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!player.isOp()) return;

        String title = event.getView().getTitle();

        // Список бізнесів
        if (title.equals("§4Адмін: §cБізнеси")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta == null) return;
            for (Business b : plugin.getBusinessManager().getAllBusinesses()) {
                if (("§e" + b.getName()).equals(meta.getDisplayName())) {
                    openBusinessSettings(player, b);
                    return;
                }
            }
        }

        // Налаштування бізнесу
        if (title.startsWith("§4Налаштування:")) {
            event.setCancelled(true);
            String bid = selectedBusiness.get(player.getUniqueId());
            if (bid == null) return;
            Business business = plugin.getBusinessManager().getBusinessById(bid);
            if (business == null) return;

            int slot = event.getSlot();

            // Назад
            if (slot == 27) { open(player); return; }

            // Видалити бізнес
            if (slot == 17) {
                player.closeInventory();
                player.sendMessage("§cВи впевнені що хочете видалити бізнес §e" + business.getName() + "§c? Введіть §4yes §cщоб підтвердити:");
                plugin.getChatInputManager().waitForInput(player, input -> {
                    if (input.equalsIgnoreCase("yes")) {
                        plugin.getHologramManager().removeHologram(business);
                        plugin.getBusinessManager().deleteBusiness(business.getId());
                        player.sendMessage("§a✔ Бізнес §e" + business.getName() + " §aвидалено!");
                    } else {
                        player.sendMessage("§7Видалення скасовано.");
                    }
                });
                return;
            }

            // Прив'язати NPC
            if (slot == 8) {
                player.closeInventory();
                player.sendMessage("§7Введіть ID NPC для прив'язки до бізнесу §e" + business.getName() + "§7:");
                plugin.getChatInputManager().waitForInput(player, input -> {
                    try {
                        int npcId = Integer.parseInt(input);
                        business.setNpcId(npcId);
                        plugin.getBusinessManager().saveBusiness(business);
                        player.sendMessage("§a✔ NPC з ID §e" + npcId + " §aприв'язано до бізнесу §e" + business.getName() + "§a!");
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНевірний ID!");
                    }
                });
                return;
            }

            // Товари магазину
            if (slot == 9) {
                openShopItems(player, business);
                return;
            }

            // Налаштування значень
            String[] fieldNames = {"зарплату (₴/год)", "% від продажів", "щоденний податок (₴)",
                "максимальний борг (₴)", "% банди", "комісію (%)", "ціну покупки (₴)"};

            if (slot >= 0 && slot <= 6) {
                int finalSlot = slot;
                player.closeInventory();
                player.sendMessage("§7Введіть нове значення для §e" + fieldNames[slot] + "§7:");
                plugin.getChatInputManager().waitForInput(player, input -> {
                    try {
                        double value = Double.parseDouble(input);
                        switch (finalSlot) {
                            case 0: business.setHourlyWage(value); break;
                            case 1: business.setSalesPercent(value); break;
                            case 2: business.setDailyTax(value); break;
                            case 3: business.setMaxTaxDebt(value); break;
                            case 4: business.setGangPercent(value); break;
                            case 5: business.setCommissionPercent(value); break;
                            case 6: business.setBuyPrice(value); break;
                        }
                        plugin.getBusinessManager().saveBusiness(business);
                        plugin.getHologramManager().updateHologram(business);
                        player.sendMessage("§a✔ Значення оновлено!");
                        openBusinessSettings(player, business);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНевірне значення!");
                    }
                });
            }
        }

        // Товари магазину
        if (title.startsWith("§6Товари:")) {
            event.setCancelled(true);
            String bid = selectedBusiness.get(player.getUniqueId());
            if (bid == null) return;
            Business business = plugin.getBusinessManager().getBusinessById(bid);
            if (business == null) return;

            int slot = event.getSlot();

            // Назад
            if (slot == 53) {
                openBusinessSettings(player, business);
                return;
            }

            // Додати товар
            if (slot == 45) {
                player.closeInventory();
                player.sendMessage("§7Введіть назву матеріалу товару (наприклад: BREAD, DIAMOND_SWORD):");
                plugin.getChatInputManager().waitForInput(player, materialStr -> {
                    try {
                        Material.valueOf(materialStr.toUpperCase());
                        player.sendMessage("§7Введіть назву товару (як буде відображатись):");
                        plugin.getChatInputManager().waitForInput(player, displayName -> {
                            player.sendMessage("§7Введіть ціну товару:");
                            plugin.getChatInputManager().waitForInput(player, priceStr -> {
                                try {
                                    double price = Double.parseDouble(priceStr);
                                    addShopItem(business, materialStr.toUpperCase(), displayName, price);
                                    player.sendMessage("§a✔ Товар §e" + displayName + " §aдодано за §e" + String.format("%.0f", price) + " ₴!");
                                    openShopItems(player, business);
                                } catch (NumberFormatException e) {
                                    player.sendMessage("§cНевірна ціна!");
                                }
                            });
                        });
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§cНевірний матеріал! Приклад: BREAD, DIAMOND_SWORD, APPLE");
                    }
                });
                return;
            }

            // ПКМ - видалити товар
            if (event.isRightClick() && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                removeShopItem(business, slot);
                player.sendMessage("§a✔ Товар видалено!");
                openShopItems(player, business);
                return;
            }

            // ЛКМ - змінити ціну
            if (event.isLeftClick() && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR
                && slot < 45) {
                player.closeInventory();
                player.sendMessage("§7Введіть нову ціну для товару:");
                plugin.getChatInputManager().waitForInput(player, priceStr -> {
                    try {
                        double price = Double.parseDouble(priceStr);
                        updateShopItemPrice(business, slot, price);
                        player.sendMessage("§a✔ Ціну оновлено!");
                        openShopItems(player, business);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНевірна ціна!");
                    }
                });
            }
        }
    }

    private void addShopItem(Business business, String material, String name, double price) {
        java.io.File shopDir = new java.io.File(plugin.getDataFolder(), "shops");
        if (!shopDir.exists()) shopDir.mkdirs();
        java.io.File shopFile = new java.io.File(shopDir, business.getId() + ".yml");
        org.bukkit.configuration.file.FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile);

        int slot = 0;
        if (config.getConfigurationSection("items") != null) {
            slot = config.getConfigurationSection("items").getKeys(false).size();
        }

        String key = "item_" + slot;
        config.set("items." + key + ".slot", slot);
        config.set("items." + key + ".material", material);
        config.set("items." + key + ".name", name);
        config.set("items." + key + ".price", price);
        try { config.save(shopFile); } catch (Exception e) { e.printStackTrace(); }
    }

    private void removeShopItem(Business business, int slot) {
        java.io.File shopFile = new java.io.File(plugin.getDataFolder(), "shops/" + business.getId() + ".yml");
        if (!shopFile.exists()) return;
        org.bukkit.configuration.file.FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile);
        if (config.getConfigurationSection("items") == null) return;
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            if (config.getInt("items." + key + ".slot") == slot) {
                config.set("items." + key, null);
                try { config.save(shopFile); } catch (Exception e) { e.printStackTrace(); }
                return;
            }
        }
    }

    private void updateShopItemPrice(Business business, int slot, double price) {
        java.io.File shopFile = new java.io.File(plugin.getDataFolder(), "shops/" + business.getId() + ".yml");
        if (!shopFile.exists()) return;
        org.bukkit.configuration.file.FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(shopFile);
        if (config.getConfigurationSection("items") == null) return;
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            if (config.getInt("items." + key + ".slot") == slot) {
                config.set("items." + key + ".price", price);
                try { config.save(shopFile); } catch (Exception e) { e.printStackTrace(); }
                return;
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
