package ua.lviv.stapixbusiness.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.lviv.stapixbusiness.StapixBusiness;

public class PlayerInteractListener implements Listener {

    private final StapixBusiness plugin;

    public PlayerInteractListener(StapixBusiness plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getBusinessManager().saveAll();
    }
}
