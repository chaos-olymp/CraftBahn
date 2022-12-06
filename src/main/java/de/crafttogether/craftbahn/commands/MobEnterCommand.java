package de.crafttogether.craftbahn.commands;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MobEnterCommand implements TabExecutor {

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String st, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mobenter")) {
            if (!(sender instanceof Player))
                return true;

            Player p = (Player) sender;
            MinecartGroup train = TCHelper.getTrain(p);

            if (train == null) {
                p.sendMessage(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &cBitte setze dich zuerst in einen Zug."));
                return true;
            }

            Location center = p.getLocation();
            int entered = 0;
            for (Entity entity : WorldUtil.getNearbyEntities(center, 5.0, 3.0, 5.0)) {
                if (entity.getVehicle() != null)
                    continue;

                if (EntityUtil.isMob(entity)) {
                    for (MinecartMember<?> member : train) {
                        if (member.getAvailableSeatCount(entity) > 0 && member.addPassengerForced(entity)) {
                            entered++;
                            break;
                        }
                    }
                }
            }

            if (entered > 0)
                p.sendMessage(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &6Es wurden &e" + entered + " Tiere &6in deinen Zug gesetzt."));
            else
                p.sendMessage(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurden keine Tiere in einem Umkreis von 5 Blöcken zu dir gefunden"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}