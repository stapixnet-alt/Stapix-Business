package ua.lviv.business.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ua.lviv.business.LvivBusiness;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {

    private final LvivBusiness plugin;
    private final Map<UUID, Consumer<String>> waitingPlayers = new HashMap<>();

    public ChatInputManager(LvivBusiness plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void waitForInput(Player player, Consumer<String> callback) {
        waitingPlayers.put(player.getUniqueId(), callback);
        player.sendMessage("§7(Введіть §ccancel §7щоб скасувати)");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = waitingPlayers.get(player.getUniqueId());
        if (callback == null) return;

        event.setCancelled(true);
        waitingPlayers.remove(player.getUniqueId());

        String message = event.getMessage();
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage("§7Скасовано.");
            return;
        }

        callback.accept(message);
    }
}
