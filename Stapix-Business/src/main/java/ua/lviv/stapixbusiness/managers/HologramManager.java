package ua.lviv.stapixbusiness.managers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.models.Business;

import java.util.Arrays;

public class HologramManager {

    private final StapixBusiness plugin;

    public HologramManager(StapixBusiness plugin) {
        this.plugin = plugin;
    }

    public void createHologram(Business business, Location location) {
        business.setHoloX(location.getX());
        business.setHoloY(location.getY());
        business.setHoloZ(location.getZ());
        business.setHoloWorld(location.getWorld().getName());
        updateHologram(business);
        plugin.getBusinessManager().saveBusiness(business);
    }

    public void updateHologram(Business business) {
        String holoId = "business_" + business.getId();
        Location loc = new Location(
            Bukkit.getWorld(business.getHoloWorld()),
            business.getHoloX(),
            business.getHoloY(),
            business.getHoloZ()
        );

        // Видаляємо стару голограму якщо є
        if (DHAPI.getHologram(holoId) != null) {
            DHAPI.removeHologram(holoId);
        }

        // Рядки голограми
        String ownerLine = business.hasOwner()
            ? "§7Власник: §e" + business.getOwnerName()
            : "§7Власник: §cНемає";

        String gangLine = business.hasGang()
            ? "§7Дах: §c" + business.getGangOwner()
            : "§7Дах: §aВільний";

        String priceLine = "§7Ціна: §a" + String.format("%.0f", business.getBuyPrice()) + " §7₴";

        String buyLine = business.hasOwner()
            ? "§cБізнес зайнятий"
            : "§aНапишіть §e/buybusiness " + business.getId() + " §aщоб купити";

        DHAPI.createHologram(holoId, loc, Arrays.asList(
            "§6§l" + business.getName(),
            ownerLine,
            gangLine,
            priceLine,
            buyLine
        ));
    }

    public void removeHologram(Business business) {
        String holoId = "business_" + business.getId();
        if (DHAPI.getHologram(holoId) != null) {
            DHAPI.removeHologram(holoId);
        }
    }
}
