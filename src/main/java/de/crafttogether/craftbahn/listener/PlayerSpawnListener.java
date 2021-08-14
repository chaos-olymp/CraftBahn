package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.UUID;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // Look if player should be a passenger
        Passenger passengerData = Passenger.get(uuid);

        // This player is not our passenger
        if (passengerData == null)
            return;

        String trainName = passengerData.getTrainName();
        int cartIndex = passengerData.getCartIndex();

        Message.debug("Try to find train '" + trainName + "' for " + player.getName() + " cartIndex: " + cartIndex);

        // Try to find train and set player as passenger
        MinecartGroup train = TCHelper.getTrain(trainName);

        if (train != null) {
            MinecartMember cart = train.get(cartIndex);

            if (cart instanceof MinecartMemberRideable) {
                if (player.isFlying())
                    player.setFlying(false);

                e.setSpawnLocation(cart.getBlock().getLocation());
                cart.getEntity().setPassenger(player);

                Message.debug("Set player " + player.getName() + " as passenger of '" + trainName + "' at cartIndex: " + cartIndex);
                Passenger.remove(uuid);
            }
            else
                Message.debug("Cart(" + cartIndex + ") at Train '" + trainName + "' is not rideable.");
        }
        else
            Message.debug("Train '" + trainName + "' was not found.");
    }
}

