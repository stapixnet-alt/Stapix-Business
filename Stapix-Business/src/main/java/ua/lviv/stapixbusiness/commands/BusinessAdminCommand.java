package ua.lviv.stapixbusiness.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.gui.BusinessAdminGUI;
import ua.lviv.stapixbusiness.models.Business;

public class BusinessAdminCommand implements CommandExecutor {

    private final StapixBusiness plugin;

    public BusinessAdminCommand(StapixBusiness plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cУ вас немає прав!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТільки гравці можуть використовувати цю команду!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            // Відкрити адмін панель
            new BusinessAdminGUI(plugin).open(player);
            return true;
        }

        // /businessadmin delete <id>
        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                player.sendMessage("§cВикористання: /businessadmin delete <id>");
                return true;
            }
            Business business = plugin.getBusinessManager().getBusinessById(args[1]);
            if (business == null) {
                player.sendMessage("§cБізнес не знайдено!");
                return true;
            }
            plugin.getHologramManager().removeHologram(business);
            plugin.getBusinessManager().deleteBusiness(args[1]);
            player.sendMessage("§a✔ Бізнес §e" + args[1] + " §aвидалено!");
            return true;
        }

        // /businessadmin create <id> <ціна> <назва>
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 4) {
                player.sendMessage("§cВикористання: /businessadmin create <id> <ціна> <назва>");
                return true;
            }
            String id = args[1];
            double price;
            try {
                price = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cНевірна ціна!");
                return true;
            }
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                nameBuilder.append(args[i]);
                if (i < args.length - 1) nameBuilder.append(" ");
            }
            String name = nameBuilder.toString();

            if (plugin.getBusinessManager().getBusinessById(id) != null) {
                player.sendMessage("§cБізнес з ID §e" + id + " §cвже існує!");
                return true;
            }

            Business business = plugin.getBusinessManager().createBusiness(id, name, price);

            // Встановити голограму на поточну позицію гравця
            Location loc = player.getLocation().add(0, 1, 0);
            plugin.getHologramManager().createHologram(business, loc);

            player.sendMessage("§a✔ Бізнес §e" + name + " §aстворено з ID §e" + id);
            player.sendMessage("§7Голограму встановлено на вашій позиції.");
            player.sendMessage("§7Використайте §e/businessadmin §7для налаштування.");
            return true;
        }

        // /businessadmin setholo <id>
        if (args[0].equalsIgnoreCase("setholo")) {
            if (args.length < 2) {
                player.sendMessage("§cВикористання: /businessadmin setholo <id>");
                return true;
            }
            Business business = plugin.getBusinessManager().getBusinessById(args[1]);
            if (business == null) {
                player.sendMessage("§cБізнес не знайдено!");
                return true;
            }
            Location loc = player.getLocation().add(0, 1, 0);
            plugin.getHologramManager().createHologram(business, loc);
            player.sendMessage("§a✔ Голограму переміщено на вашу позицію!");
            return true;
        }

        // /businessadmin list
        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage("§6════ §eСписок бізнесів §6════");
            for (Business b : plugin.getBusinessManager().getAllBusinesses()) {
                String owner = b.hasOwner() ? "§e" + b.getOwnerName() : "§7Немає";
                player.sendMessage("§7• §f" + b.getName() + " §7(§f" + b.getId() + "§7) - " + owner);
            }
            return true;
        }

        player.sendMessage("§cКоманди: create, setholo, list або /businessadmin для панелі");
        return true;
    }
}
