package ua.lviv.business.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.gui.BusinessOwnerGUI;
import ua.lviv.business.models.Business;

public class BusinessCommand implements CommandExecutor {

    private final LvivBusiness plugin;

    public BusinessCommand(LvivBusiness plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТільки гравці можуть використовувати цю команду!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            // Показати список своїх бізнесів
            boolean hasAny = false;
            player.sendMessage("§6════ §eВаші бізнеси §6════");
            for (Business b : plugin.getBusinessManager().getAllBusinesses()) {
                if (b.hasOwner() && b.getOwnerUUID().equals(player.getUniqueId())) {
                    player.sendMessage("§7• §e" + b.getName() + " §7(ID: §f" + b.getId() + "§7)");
                    hasAny = true;
                }
            }
            if (!hasAny) {
                player.sendMessage("§7У вас немає бізнесів.");
            }
            player.sendMessage("§7Використайте: §e/business <id> §7для управління");
            return true;
        }

        String businessId = args[0];
        Business business = plugin.getBusinessManager().getBusinessById(businessId);

        if (business == null) {
            player.sendMessage("§cБізнес не знайдено!");
            return true;
        }

        if (!business.hasOwner() || !business.getOwnerUUID().equals(player.getUniqueId())) {
            if (!player.isOp()) {
                player.sendMessage("§cЦе не ваш бізнес!");
                return true;
            }
        }

        new BusinessOwnerGUI(plugin, business).open(player, business);
        return true;
    }
}
