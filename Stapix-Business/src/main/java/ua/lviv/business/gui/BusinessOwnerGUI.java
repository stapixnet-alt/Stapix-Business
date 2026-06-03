package ua.lviv.business.gui;

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
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.models.Business;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BusinessOwnerGUI implements Listener {

    private static BusinessOwnerGUI instance;
    private final LvivBusiness plugin;
    private static final Map<UUID, Business> openGUIs = new HashMap<>();

    public BusinessOwnerGUI(LvivBusiness plugin, Business business) {
        this.plugin = plugin;
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Business business = openGUIs.get(player.getUniqueId());
        if (business == null) return;
        openWithBusiness(player, business);
    }

    public void open(Player player, Business business) {
        openGUIs.put(player.getUniqueId(), business);
        openWithBusiness(player, business);
    }

    private void openWithBusiness(Player player, Business business) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Бізнес: §e" + business.getName());

        inv.setItem(11, createItem(Material.GOLD_INGOT, "§eБаланс бізнесу",
            "§7Поточний баланс: §a" + String.format("%.0f", business.getBusinessBalance()) + " ₴",
            "§7Зарплата: §a" + String.format("%.0f", business.getHourlyWage()) + " ₴/год",
            "§7% від продажів: §a" + business.getSalesPercent() + "%",
            "",
            "§aНатисніть щоб зняти кошти"
        ));

        inv.setItem(13, createItem(Material.PAPER, "§cПодатки",
            "§7Борг: §c" + String.format("%.0f", business.getTaxDebt()) +
                " §7/ §c" + String.format("%.0f", business.getMaxTaxDebt()) + " ₴",
            "§7Щоденний податок: §c" + String.format("%.0f", business.getDailyTax()) + " ₴",
            "",
            "§aНатисніть щоб сплатити"
        ));

        inv.setItem(15, createItem(Material.EMERALD, "§aПродати гравцю",
            "§7Мінімальна ціна: §e" + String.format("%.0f", business.getBuyPrice()) + " ₴",
            "",
            "§aНатисніть щоб продати"
        ));

        inv.setItem(16, createItem(Material.IRON_NUGGET, "§7Продати місту",
            "§7Ціна викупу: §e" + String.format("%.0f", business.getBuyPrice()) + " ₴",
            "",
            "§aНатисніть щоб продати місту"
        ));

        String gangInfo = business.hasGang() ? "§c" + business.getGangOwner() : "§aВільний";
        inv.setItem(4, createItem(Material.IRON_SWORD, "§cДах",
            "§7Поточний дах: " + gangInfo,
            "§7% від доходу: §c" + business.getGangPercent() + "%"
        ));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().startsWith("§6Бізнес:")) return;
        event.setCancelled(true);

        Business business = openGUIs.get(player.getUniqueId());
        if (business == null) return;

        int slot = event.getSlot();

        if (slot == 11) {
            player.closeInventory();
            double balance = business.getBusinessBalance();
            if (balance <= 0) {
                player.sendMessage("§cНа балансі бізнесу немає коштів!");
                return;
            }
            plugin.getBusinessManager().getEconomy().depositPlayer(player, balance);
            business.setBusinessBalance(0);
            plugin.getBusinessManager().saveBusiness(business);
            player.sendMessage("§a✔ Ви зняли §e" + String.format("%.0f", balance) + " ₴ §aз балансу бізнесу!");

        } else if (slot == 13) {
            player.closeInventory();
            if (business.getTaxDebt() <= 0) {
                player.sendMessage("§aУ вас немає податкового боргу!");
                return;
            }
            player.sendMessage("§7Введіть суму для сплати податку (борг: §e" +
                String.format("%.0f", business.getTaxDebt()) + " ₴§7):");
            plugin.getChatInputManager().waitForInput(player, input -> {
                try {
                    double amount = Double.parseDouble(input);
                    plugin.getTaxManager().payTax(player, business, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cНевірна сума!");
                }
            });

        } else if (slot == 15) {
            player.closeInventory();
            player.sendMessage("§7Введіть нік гравця якому хочете продати бізнес:");
            plugin.getChatInputManager().waitForInput(player, targetName -> {
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    player.sendMessage("§cГравець не знайдено або не онлайн!");
                    return;
                }
                player.sendMessage("§7Введіть ціну продажу (мін: §e" +
                    String.format("%.0f", business.getBuyPrice()) + " ₴§7):");
                plugin.getChatInputManager().waitForInput(player, priceStr -> {
                    try {
                        double price = Double.parseDouble(priceStr);
                        if (price < business.getBuyPrice()) {
                            player.sendMessage("§cЦіна не може бути меншою за §e" +
                                String.format("%.0f", business.getBuyPrice()) + " ₴");
                            return;
                        }
                        int maxB = plugin.getBusinessManager().getMaxBusinesses(target);
                        int owned = plugin.getBusinessManager().countOwnedBusinesses(target.getUniqueId());
                        if (owned >= maxB) {
                            player.sendMessage("§cУ цього гравця вже максимальна кількість бізнесів!");
                            return;
                        }
                        if (plugin.getBusinessManager().getEconomy().getBalance(target) < price) {
                            player.sendMessage("§cУ гравця недостатньо коштів!");
                            return;
                        }
                        double commission = price * (business.getCommissionPercent() / 100.0);
                        double sellerGets = price - commission;
                        plugin.getBusinessManager().getEconomy().withdrawPlayer(target, price);
                        plugin.getBusinessManager().getEconomy().depositPlayer(player, sellerGets);
                        business.setOwnerUUID(target.getUniqueId());
                        business.setOwnerName(target.getName());
                        business.setBusinessBalance(0);
                        business.setTaxDebt(0);
                        business.setTaxDebtReachedMaxTime(0);
                        plugin.getBusinessManager().saveBusiness(business);
                        plugin.getHologramManager().updateHologram(business);
                        player.sendMessage("§a✔ Бізнес продано гравцю §e" + target.getName() + "§a!");
                        target.sendMessage("§a✔ Ви придбали бізнес §e" + business.getName() + "§a!");
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНевірна ціна!");
                    }
                });
            });

        } else if (slot == 16) {
            player.closeInventory();
            double cityPrice = business.getBuyPrice();
            plugin.getBusinessManager().getEconomy().depositPlayer(player, cityPrice);
            plugin.getBusinessManager().removeBusiness(business);
            openGUIs.remove(player.getUniqueId());
            player.sendMessage("§a✔ Бізнес продано місту за §e" + String.format("%.0f", cityPrice) + " ₴");
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
