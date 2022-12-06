package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class TrainEnterListener implements Listener {

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        Player p = TCHelper.getPlayer(e.getEntered());
        if (p == null) return;

        MinecartMember<?> cart = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (cart == null) return;

        // Set new enterMessage
        String enterMessage = cart.getProperties().getEnterMessage();
        if (enterMessage.equalsIgnoreCase("cbDefault")) {
            cart.getProperties().setEnterMessage("");
            sendEnterMessage(p, cart);
        }

        /* Set View-Distance */
        //p.setNoTickViewDistance(6);
        //p.setViewDistance(6);
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        /*
        Player p = TCHelper.getPlayer(e.getEntered());
        if (p == null) return;

        MinecartMember<?> cart = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (cart == null) return;
        */

        /* Set View-Distance */
        //p.setNoTickViewDistance(p.getWorld().getNoTickViewDistance());
        //p.setViewDistance(p.getWorld().getViewDistance());
    }

    private void sendEnterMessage(Player p, MinecartMember<?> cart) {
        TextComponent message;

        if (cart.getProperties().getDestination().isEmpty()) {
            message = new TextComponent(Message.format("&8[&e&l!&8] &b&lCraftBahn &8»"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &c&lHinweis:"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &cDieser Zug hat noch kein Fahrziel."));

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Verfügbare Fahrziele anzeigen"))).create()));
            p.spigot().sendMessage(message);
        } else {
            message = new TextComponent(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» "));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &eDieser Zug versucht, das Ziel:"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &e'&e&l" + cart.getProperties().getDestination() + "&e' &ezu erreichen."));

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Anderes Fahrziel auswählen")).create())));
            p.spigot().sendMessage(message);
        }

        message = new TextComponent(Message.newLine());
        message.addExtra(Message.format(""));
        p.spigot().sendMessage(message);
    }
}