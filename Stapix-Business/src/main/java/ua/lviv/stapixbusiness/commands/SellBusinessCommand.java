package ua.lviv.stapixbusiness.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.models.Business;

public class SellBusinessCommand implements CommandExecutor {

    private final StapixBusiness plugin;

    public SellBusinessCommand(StapixBusiness plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        player.sendMessage("§7Використайте §e/business <id> §7для продажу бізнесу через меню.");
        return true;
    }
}
