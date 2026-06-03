package ua.lviv.stapixbusiness.listeners;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ua.lviv.stapixbusiness.StapixBusiness;
import ua.lviv.stapixbusiness.gui.NPCShopGUI;
import ua.lviv.stapixbusiness.models.Business;

public class NPCClickListener implements Listener {

    private final StapixBusiness plugin;

    public NPCClickListener(StapixBusiness plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int npcId = event.getNPC().getId();

        for (Business business : plugin.getBusinessManager().getAllBusinesses()) {
            if (business.getNpcId() == npcId) {
                new NPCShopGUI(plugin, business).open(player, business);
                return;
            }
        }
    }
}
