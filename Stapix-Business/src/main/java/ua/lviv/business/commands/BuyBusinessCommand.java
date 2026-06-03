package ua.lviv.business.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.models.Business;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyBusinessCommand implements CommandExecutor {

    private final LvivBusiness plugin;
    private final Map<UUID, String> pendingPurchases = new HashMap<>();

    public BuyBusinessCommand(LvivBusiness plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТільки гравці можуть використовувати цю команду!");
            return true;
        }
        Player player = (Player) sender;

        // Підтвердження через yes/no
        if (args.length > 0 && (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no"))) {
            String businessId = pendingPurchases.get(player.getUniqueId());
            if (businessId == null) {
                player.sendMessage("§cНемає активного запиту на покупку!");
                return true;
            }
            if (args[0].equalsIgnoreCase("no")) {
                pendingPurchases.remove(player.getUniqueId());
                player.sendMessage("§7Покупку скасовано.");
                return true;
            }
            // Підтвердження yes
            Business business = plugin.getBusinessManager().getBusinessById(businessId);
            if (business == null || business.hasOwner()) {
                player.sendMessage("§cЦей бізнес вже куплений або не існує!");
                pendingPurchases.remove(player.getUniqueId());
                return true;
            }
            if (plugin.getBusinessManager().getEconomy().getBalance(player) < business.getBuyPrice()) {
                player.sendMessage("§cНедостатньо коштів!");
                pendingPurchases.remove(player.getUniqueId());
                return true;
            }
            plugin.getBusinessManager().buyBusiness(player, business);
            pendingPurchases.remove(player.getUniqueId());
            player.sendMessage("§a✔ Ви успішно придбали бізнес §e" + business.getName() + "§a!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cВикористання: /buybusiness <id>");
            return true;
        }

        String businessId = args[0];
        Business business = plugin.getBusinessManager().getBusinessById(businessId);

        if (business == null) {
            player.sendMessage("§cБізнес з ID §e" + businessId + " §cне знайдено!");
            return true;
        }

        if (business.hasOwner()) {
            player.sendMessage("§cЦей бізнес вже має власника: §e" + business.getOwnerName());
            return true;
        }

        // Перевірка відстані до голограми (3 блоки)
        if (business.getHoloWorld() != null && !business.getHoloWorld().isEmpty()) {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(business.getHoloWorld());
            if (world != null && player.getWorld().equals(world)) {
                org.bukkit.Location holoLoc = new org.bukkit.Location(world, business.getHoloX(), business.getHoloY(), business.getHoloZ());
                if (player.getLocation().distance(holoLoc) > 3) {
                    player.sendMessage("§cВи занадто далеко від бізнесу! Підійдіть ближче (до 3 блоків).");
                    return true;
                }
            }
        }

        int maxBusinesses = plugin.getBusinessManager().getMaxBusinesses(player);
        int owned = plugin.getBusinessManager().countOwnedBusinesses(player.getUniqueId());
        if (owned >= maxBusinesses) {
            player.sendMessage("§cВи вже маєте максимальну кількість бізнесів (§e" + maxBusinesses + "§c)!");
            return true;
        }

        if (plugin.getBusinessManager().getEconomy().getBalance(player) < business.getBuyPrice()) {
            player.sendMessage("§cНедостатньо коштів! Потрібно: §e" +
                String.format("%.0f", business.getBuyPrice()) + " ₴");
            return true;
        }

        pendingPurchases.put(player.getUniqueId(), businessId);
        player.sendMessage("§6════════════════════════");
        player.sendMessage("§e🏪 Купівля бізнесу: §f" + business.getName());
        player.sendMessage("§7Ціна: §a" + String.format("%.0f", business.getBuyPrice()) + " ₴");
        player.sendMessage("§7Щогодинна зарплата: §a" + String.format("%.0f", business.getHourlyWage()) + " ₴");
        player.sendMessage("§7Щоденний податок: §c" + String.format("%.0f", business.getDailyTax()) + " ₴");
        player.sendMessage("§aНапишіть §eyes §aщоб підтвердити або §eno §cщоб скасувати");
        player.sendMessage("§6════════════════════════");
        return true;
    }
}
