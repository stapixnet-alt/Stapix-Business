package ua.lviv.business.listeners;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ua.lviv.business.LvivBusiness;
import ua.lviv.business.gui.NPCShopGUI;
import ua.lviv.business.models.Business;

public class NPCClickListener implements Listener {

    private final LvivBusiness plugin;

    public NPCClickListener(LvivBusiness plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int npcId = event.getNPC().getId();

        // Знайти бізнес по ID NPC
        for (Business business : plugin.getBusinessManager().getAllBusinesses()) {
            if (business.getNpcId() == npcId) {
                new NPCShopGUI(plugin, business).open(player);
                return;
            }
        }
    }
}
