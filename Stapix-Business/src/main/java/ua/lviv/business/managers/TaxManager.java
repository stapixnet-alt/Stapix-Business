package ua.lviv.business.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.models.Business;

public class TaxManager {

    private final LvivBusiness plugin;

    public TaxManager(LvivBusiness plugin) {
        this.plugin = plugin;
    }

    public void startTaxTimer() {
        // Кожні 24 години нараховуємо податок
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Business b : plugin.getBusinessManager().getAllBusinesses()) {
                if (!b.hasOwner()) continue;

                b.addTaxDebt(b.getDailyTax());

                // Якщо досяг максимуму
                if (b.getTaxDebt() >= b.getMaxTaxDebt()) {
                    if (b.getTaxDebtReachedMaxTime() == 0) {
                        b.setTaxDebtReachedMaxTime(System.currentTimeMillis());
                        // Повідомити власника
                        Player owner = Bukkit.getPlayer(b.getOwnerUUID());
                        if (owner != null) {
                            owner.sendMessage("§c⚠ Ваш бізнес §e" + b.getName() +
                                " §cдосяг максимального боргу! У вас є 24 години щоб сплатити податок!");
                        }
                    } else {
                        // Перевіряємо чи пройшло 24 години
                        long elapsed = System.currentTimeMillis() - b.getTaxDebtReachedMaxTime();
                        if (elapsed >= 24L * 60 * 60 * 1000) {
                            // Забираємо бізнес
                            Player owner = Bukkit.getPlayer(b.getOwnerUUID());
                            if (owner != null) {
                                owner.sendMessage("§c❌ Ваш бізнес §e" + b.getName() +
                                    " §cбуло вилучено через несплату податків!");
                            }
                            plugin.getBusinessManager().removeBusiness(b);
                        }
                    }
                }

                plugin.getBusinessManager().saveBusiness(b);
            }
        }, 1728000L, 1728000L); // кожні 24 години
    }

    public boolean payTax(Player player, Business business, double amount) {
        double currentDebt = business.getTaxDebt();
        if (amount <= 0) {
            player.sendMessage("§cСума має бути більше 0!");
            return false;
        }
        if (amount > currentDebt) {
            player.sendMessage("§cВи не можете заплатити більше ніж ваш борг! Борг: §e" +
                String.format("%.0f", currentDebt) + " ₴");
            return false;
        }
        if (plugin.getBusinessManager().getEconomy().getBalance(player) < amount) {
            player.sendMessage("§cНедостатньо коштів!");
            return false;
        }

        plugin.getBusinessManager().getEconomy().withdrawPlayer(player, amount);
        business.setTaxDebt(currentDebt - amount);

        if (business.getTaxDebt() <= 0) {
            business.setTaxDebt(0);
            business.setTaxDebtReachedMaxTime(0);
        }

        plugin.getBusinessManager().saveBusiness(business);
        player.sendMessage("§a✔ Сплачено §e" + String.format("%.0f", amount) +
            " ₴ §aподатку. Залишок боргу: §e" +
            String.format("%.0f", business.getTaxDebt()) + " ₴");
        return true;
    }
}
