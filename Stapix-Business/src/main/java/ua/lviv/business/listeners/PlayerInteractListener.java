package ua.lviv.business.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.lviv.business.LvivBusiness;

public class PlayerInteractListener implements Listener {

    private final LvivBusiness plugin;

    public PlayerInteractListener(LvivBusiness plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getBusinessManager().saveAll();
    }
}
