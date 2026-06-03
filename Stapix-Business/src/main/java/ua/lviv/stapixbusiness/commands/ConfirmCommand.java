package ua.lviv.stapixbusiness.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.lviv.stapixbusiness.StapixBusiness;

public class ConfirmCommand implements CommandExecutor {

    private final StapixBusiness plugin;
    private final boolean confirm;

    public ConfirmCommand(StapixBusiness plugin, boolean confirm) {
        this.plugin = plugin;
        this.confirm = confirm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        BuyBusinessCommand buyCmd = plugin.getBuyBusinessCommand();
        if (confirm) {
            buyCmd.confirmPurchase(player);
        } else {
            buyCmd.cancelPurchase(player);
        }
        return true;
    }
}
