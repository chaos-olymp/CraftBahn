package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Look if player should be a passenger
        Passenger passenger = Passenger.get(e.getPlayer().getUniqueId());

        if (passenger != null)
            PortalHandler.reEnterPassenger(passenger, e);
        else {
            Message.debug(e.getPlayer().getName() + " -> Is not a passenger");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        // Spawn player at correct location
        Passenger passenger = Passenger.get(e.getPlayer().getUniqueId());

        if (passenger != null) {
            MinecartGroup train = TCHelper.getTrain(passenger.getTrainId());

            if (train == null)
                return;

            e.setSpawnLocation(train.get(passenger.getCartIndex()).getEntity().getLocation());
        }
    }
}

