package ua.lviv.business.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.models.Business;

import java.util.List;

public class ClaimBusinessCommand implements CommandExecutor {

    private final LvivBusiness plugin;

    public ClaimBusinessCommand(LvivBusiness plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("business.gang.claim")) {
            player.sendMessage("§cУ вас немає прав! Тільки лідери банд можуть захоплювати бізнеси.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cВикористання: /claimbusiness <id>");
            return true;
        }

        Business business = plugin.getBusinessManager().getBusinessById(args[0]);
        if (business == null) {
            player.sendMessage("§cБізнес не знайдено!");
            return true;
        }

        // Визначити банду гравця
        String gangName = getPlayerGang(player);
        if (gangName == null) {
            player.sendMessage("§cВи не є лідером жодної банди!");
            return true;
        }

        // Якщо даху немає — одразу захопити
        if (!business.hasGang()) {
            business.setGangOwner(gangName);
            plugin.getBusinessManager().saveBusiness(business);
            plugin.getHologramManager().updateHologram(business);
            player.sendMessage("§a✔ Банда §e" + gangName + " §aтепер тримає дах над §e" + business.getName() + "§a!");
            return true;
        }

        // Якщо це вже ваша банда
        if (business.getGangOwner().equals(gangName)) {
            player.sendMessage("§cВаша банда вже тримає дах над цим бізнесом!");
            return true;
        }

        // Перевірка можливості стрілки
        if (!business.canRetryStreetFight() && business.getLastStreetFightTime() != 0) {
            long remainMs = (24L * 60 * 60 * 1000) - (System.currentTimeMillis() - business.getLastStreetFightTime());
            long remainHours = remainMs / (60 * 60 * 1000);
            long remainMins = (remainMs % (60 * 60 * 1000)) / (60 * 1000);
            player.sendMessage("§cСтрілка за цей бізнес можлива через §e" + remainHours + "г " + remainMins + "хв");
            return true;
        }

        // Показати доступні години
        List<Integer> allowedHours = plugin.getGangManager().getAllowedFightHours();
        if (allowedHours.isEmpty()) {
            player.sendMessage("§cАдмін не встановив дозволені години стрілок!");
            return true;
        }

        StringBuilder hoursStr = new StringBuilder();
        for (int h : allowedHours) {
            hoursStr.append(h).append(":00, ");
        }

        player.sendMessage("§6════════════════════════");
        player.sendMessage("§e⚔ Стрілка за бізнес: §f" + business.getName());
        player.sendMessage("§7Поточний дах: §c" + business.getGangOwner());
        player.sendMessage("§7Доступні години: §e" + hoursStr.toString().replaceAll(", $", ""));
        player.sendMessage("§7Введіть годину для стрілки (наприклад: §e18§7):");
        player.sendMessage("§6════════════════════════");

        plugin.getChatInputManager().waitForInput(player, hourStr -> {
            try {
                int hour = Integer.parseInt(hourStr);
                if (!allowedHours.contains(hour)) {
                    player.sendMessage("§cЦя година не дозволена для стрілок!");
                    return;
                }
                // Записати заплановану стрілку
                business.setStreetFightScheduledTime(hour);
                business.setLastStreetFightTime(System.currentTimeMillis());
                plugin.getBusinessManager().saveBusiness(business);

                player.sendMessage("§a✔ Стрілку призначено на §e" + hour + ":00§a!");
                player.sendMessage("§7Банда §c" + business.getGangOwner() + " §7буде повідомлена.");

                // Повідомити лідера ворожої банди (якщо онлайн)
                // TODO: додати сповіщення лідеру ворожої банди

            } catch (NumberFormatException e) {
                player.sendMessage("§cНевірна година!");
            }
        });

        return true;
    }

    private String getPlayerGang(Player player) {
        if (player.hasPermission("gang.leader.koty")) return "Koty";
        if (player.hasPermission("gang.leader.mukhy")) return "Mukhy";
        return null;
    }
}
