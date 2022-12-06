package de.crafttogether.craftbahn.commands;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationList;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.MarkerManager;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Commands implements TabExecutor {

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String st, String[] args) {
        DestinationStorage destinationStorage = CraftBahnPlugin.getInstance().getDestinationStorage();

        Player p = null;

        if (cmd.getName().equalsIgnoreCase("rbf") || cmd.getName().equalsIgnoreCase("rt") || cmd.getName().equalsIgnoreCase("rtp")) {
            List<Destination> list = new ArrayList<>();

            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            boolean showAll = args.length > 0 && args[0].equalsIgnoreCase("all");

            List<Destination> destinations = new ArrayList<>(destinationStorage.getDestinations());
            destinations = destinationStorage.filterByServer(destinations, CraftBahnPlugin.getInstance().getServerName());

            for (Destination dst : destinations) {
                if (showAll) {
                    list.add(dst);
                } else if (dst.getType().equals(Destination.DestinationType.STATION))
                    list.add(dst);
            }

            if (list.size() > 0) {
                Random rand = new Random();
                Destination dest = list.get(rand.nextInt(list.size()));

                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDu wurdest zum " + dest.getType() + " &e'&e" + dest.getName() + "'&e &cteleportiert.");
                p.teleport(dest.getTeleportLocation().getBukkitLocation());
            }
            else
                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein mögliches Ziel gefunden.");
        }

        else if (cmd.getName().equalsIgnoreCase("fahrziel")) {
            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            if (args.length == 0) {
                sendMessage(p, "");

                TextComponent message = new TextComponent();
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Verwendung: &e/fahrziele oder /fahrziel <Name>"));
                p.spigot().sendMessage(message);

                return true;
            }

            String serverName = null;
            Integer page = null;

            List<Destination> found = new ArrayList<>();
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                }
                catch(Exception ignored) {}

                if (page == null)
                    serverName = args[1];
            }

            if (serverName != null)
                found.add(destinationStorage.getDestination(args[0], serverName));
            else
                found = new ArrayList<>(destinationStorage.getDestinations(args[0]));

            if (found.size() < 1) {
                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit dem Namen &c'&c" + args[0] + "&c'" + (serverName != null ? " &cauf dem angegebenen Server." : ""));
                return true;
            }

            else if (found.size() > 1) {
                DestinationList list = new DestinationList(found);
                list.setFilterName(args[0]);
                list.showOwner(true);
                list.showLocation(true);
                list.showFooter(true);

                sendMessage(p, "");

                if (page == null) {
                    p.sendMessage(Message.newLine());
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &aEs wurden mehrere mögliche Ziele gefunden:");
                }

                list.sendPage(p, (page == null ? 1 : page));
                return true;
            }

            Destination dest = found.get(0);

            if (!dest.isPublic() && !p.hasPermission("ctdestinations.see.private")) {
                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cAuf dieses Ziel hast du keinen Zugriff.");
                return true;
            }

            MinecartGroup train = TCHelper.getTrain(p);

            if (train == null) {
                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cBitte setze dich zuerst in einen Zug.");
                return true;
            }

            train.getProperties().setDestination("train destination " + dest.getServer());

            if (!CraftBahnPlugin.getInstance().getServerName().equalsIgnoreCase(dest.getServer())) {
                // Create a route first
                List<String> route = new ArrayList<>();
                route.add(dest.getServer());
                route.add(dest.getName());

                train.getProperties().setDestinationRoute(route);
                train.getProperties().setDestination(dest.getServer());
            } else {
                train.getProperties().setDestination(dest.getName());
            }

            sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &aFahrziel gesetzt");

            return true;
        }

        else if (cmd.getName().equalsIgnoreCase("fahrzieledit") || cmd.getName().equalsIgnoreCase("fze")) {
            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            if (args.length == 0) {
                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &eAlle Befehle");
                sendMessage(p, "&7/fahrziel &7<name> [server] &8- &eFahrziel festlegen");
                sendMessage(p, "&7/fahrziele &7[kategorie] [seite] &8- &eFahrziele auflisten");
                sendMessage(p, "&7/fze info &7<name> [server] &8- &eDetails zu Fahrziel anzeigen");
                sendMessage(p, "&7/fze add &7<name> [typ] [sichtbarkeit] &8- &eNeues Fahrziel anlegen");
                sendMessage(p, "&7/fze remove &7<name> [server] &8- &eFahrziel entfernen");
                sendMessage(p, "&7/fze settype &7<name> [server] <typ> &8- &eKategorie aktualisieren");
                sendMessage(p, "&7/fze setowner &7<name> [server] &8- &eBesitzer aktualisieren");
                sendMessage(p, "&7/fze addmember &7<name> [server] &8- &eWeitere Besitzer hinzufügen");
                sendMessage(p, "&7/fze removemember &7<name> [server] &8- &eWeitere Besitzer entfernen");
                sendMessage(p, "&7/fze setlocation &7<name> &8- &e(Marker-)Position aktualisieren");
                sendMessage(p, "&7/fze settplocation &7<name> &8- &eTeleport-Position aktualisieren");
                sendMessage(p, "&7/fze setprivate &7<name> [server] &8- &eFahrziel verstecken");
                sendMessage(p, "&7/fze setpublic &7<name> [server] &8- &eFahrziel wieder anzeigen");
                sendMessage(p, "&7/fze tp &7<name> [server] &8- &eTeleportiere zu Fahrziel");
                sendMessage(p, "&7/fze updatemarker &7<name> [server] &8- &eAktualisiere Dynmap-Marker");
                sendMessage(p, "&7/fze updateportals &8- &eAktualisiere Portale");
            }

            else if (args[0].equalsIgnoreCase("info")) {
                if (!p.hasPermission("ctdestinations.edit.remove")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2 || args[1].equals("") || args[1].length() < 1) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cBitte gebe den Namen des Ziel ein.");
                    return true;
                }

                String serverName = CraftBahnPlugin.getInstance().getServerName();
                if (args.length >= 3)
                    serverName = args[2];

                Destination dest = destinationStorage.getDestination(args[1], serverName);
                if (dest == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen.");
                    return true;
                }

                StringBuilder strParticipants = new StringBuilder();
                for (UUID uuid : dest.getParticipants()) {
                    OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                    if (!participant.hasPlayedBefore()) continue;
                    strParticipants.append(participant.getName()).append(", ");
                }

                if (strParticipants.length() > 1)
                    strParticipants = new StringBuilder(strParticipants.substring(0, strParticipants.length() - 2));

                TextComponent message = Message.format("");
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Fahrziel: &e" + dest.getName()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7ID: &e" + dest.getId()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Typ: &e" + dest.getType()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Besitzer: &e" + dest.getType()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Mitwirkend: &e" + strParticipants));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Server: &e" + dest.getServer()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7Welt: &e" + dest.getWorld()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&8[&e&l!&8] &b&lCraftBahn &8» &7X: &e" + dest.getLocation().getX() + "&7, Y: &e" + dest.getLocation().getY() + "&7, Z: &e" + dest.getLocation().getZ()));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format(""));

                p.sendMessage(message);
            }

            else if (args[0].equalsIgnoreCase("add")) {
                boolean isPublic = true;

                if (!p.hasPermission("ctdestinations.edit.add")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) != null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert bereits ein Ziel mit diesem Namen in der Liste.");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Stationstyp angegeben");
                    return true;
                }

                if (args.length == 4 && (args[3].equalsIgnoreCase("private") || args[3].equalsIgnoreCase("hidden")))
                    isPublic = false;

                Destination.DestinationType type = null;

                try {
                    type = Destination.DestinationType.valueOf(args[2].toUpperCase());
                } catch (Exception ignored) {}

                if (type == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cUngültiger Stationstyp.");
                    return true;
                }

                Player finalP = p;
                destinationStorage.addDestination(args[1], p.getUniqueId(), type, p.getLocation(), isPublic, (err, dest) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» Es trat ein Fehler beim speichern des Fahrziel auf. Bitte kontaktiere einen Administrator.");
                    else {
                        MarkerManager.addMarker(dest);
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &aFahrziel wurde erstellt. ID: " + dest.getId());
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("remove")) {
                if (!p.hasPermission("ctdestinations.edit.remove")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2 || args[1].equals("") || args[1].length() < 1) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cBitte gebe den Namen des Ziel ein.");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                if (dest == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen.");
                    return true;
                }

                Player finalP = p;
                destinationStorage.delete(dest.getId(), (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        MarkerManager.deleteMarker(dest);
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &aZiel gelöscht.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("setowner")) {
                if (!p.hasPermission("ctdestinations.edit.setowner")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben.");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Spieler angegeben");
                    return true;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
                if (owner == null || !owner.hasPlayedBefore()) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Spieler mit dem Namen &e" + args[2] + " &cgefunden");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.setOwner(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &e'&e" + dest.getName() + "&e' &6gehört nun &e" + owner.getName());
                });
            }

            else if (args[0].equalsIgnoreCase("addmember")) {
                if (!p.hasPermission("ctdestinations.edit.addmember")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &6CraftBahn &cEs wurde kein Ziel angegeben.");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Spieler angegeben");
                    return true;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
                if (owner == null || !owner.hasPlayedBefore()) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Spieler mit dem Namen &e" + args[2] + " &cgefunden");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.addParticipant(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &6Du hast &e" + owner.getName() + " &6als Besitzer des Fahrziel &e'&e" + dest.getName() + "&e' &2hinzugefügt.");
                });
            }

            else if (args[0].equalsIgnoreCase("removemember")) {
                if (!p.hasPermission("ctdestinations.edit.removemember")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &8&cEs wurde kein Ziel angegeben.");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Spieler angegeben");
                    return true;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
                if (owner == null || !owner.hasPlayedBefore()) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Spieler mit dem Namen &e" + args[2] + " &cgefunden");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.removeParticipant(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cDu hast &e" + owner.getName() + " &cals Besitzer des Fahrziel &e'&e" + dest.getName() + "&e' &centfernt.");
                });
            }

            else if (args[0].equalsIgnoreCase("settype")) {
                if (!p.hasPermission("ctdestinations.edit.settype")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }
                if (args.length < 3) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Stationstyp angegeben");
                    return true;
                }

                Destination.DestinationType type = null;
                try {
                    type = Destination.DestinationType.valueOf(args[2].toUpperCase());
                } catch (Exception exception) {}

                if (type == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cUngültiger Stationstyp.");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.setType(type);

                // Speichern
                Player finalP = p;
                Destination.DestinationType finalType = type;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &e'" + dest.getName() + "&e' &6ist nun ein &e" + finalType);
                });
            }

            else if (args[0].equalsIgnoreCase("setlocation")) {
                if (!p.hasPermission("ctdestinations.edit.setlocation")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.setLocation(CTLocation.fromBukkitLocation(p.getLocation()));

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        MarkerManager.addMarker(dest);
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &6Du hast die Position für das Ziel: &e'&e" + dest.getName() + "&e' &6aktualisiert.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("setwarp")) {
                if (!p.hasPermission("ctdestinations.edit.setwarp")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.setTeleportLocation(CTLocation.fromBukkitLocation(p.getLocation()));

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &6Du hast die Position für das Ziel: &e'&e" + dest.getName() + "&e' &6aktualisiert.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("setprivate")) {
                if (!p.hasPermission("ctdestinations.edit.setprivate")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.setPublic(Boolean.valueOf(false));

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &6Das Ziel: &e'&e" + dest.getName() + "'&e &6ist nun &eprivat");
                    }
                });
            }
            else if (args[0].equalsIgnoreCase("setpublic")) {
                if (!p.hasPermission("ctdestinations.edit.setpublic")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                dest.setPublic(Boolean.valueOf(true));

                // Speichern
                Player finalP = p;
                destinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &6Das Ziel: &e'&e" + dest.getName() + "&e' &eist nun &eöffentlich.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("updatemarker")) {
                if (!p.hasPermission("ctdestinations.edit.updatemarker")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                MarkerManager.createMarkerSets();
                Collection<Destination> destinations = destinationStorage.getDestinations();

                for (Destination dest : destinations)
                    MarkerManager.addMarker(dest, true);

                sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &6Es wurden &e" + destinations.size() + " &6Marker erneuert.");
            }

            else if (args[0].equalsIgnoreCase("reload")) {
                if (!p.hasPermission("ctdestinations.edit.reload")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                Player finalP = p;
                destinationStorage.loadAll((err, destinations) -> {
                    if (err != null)
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &cBeim laden der Fahrziele ist ein Fehler aufgetreten");
                    else
                        sendMessage(finalP, "&8[&e&l!&8] &b&lCraftBahn &8» &2Es wurden &a" + destinations.size() + " &2Fahrziele geladen.");
                });
            }

            else if (args[0].equalsIgnoreCase("tp")) {
                if (!p.hasPermission("ctdestinations.teleport")) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDazu hast du keine Rechte");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName()) == null) {
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());

                if (dest.getTeleportLocation().getServer().equalsIgnoreCase(CraftBahnPlugin.getInstance().getServerName())) {
                    Location loc = dest.getTeleportLocation().getBukkitLocation();
                    p.teleport(loc);
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &6Du wurdest zum Ziel: &e'&e" + dest.getName() + "&e' &6teleportiert.");
                }
                else
                    sendMessage(p, "&8[&e&l!&8] &b&lCraftBahn &8» &cDas Ziel befindet sich auf einem anderen Server.");
            }
        }
        return true;
    }

    private void sendMessage(Player p, String message) {
        if (p.isOnline())
            p.sendMessage(Message.format(message));
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        DestinationStorage destinationStorage = CraftBahnPlugin.getInstance().getDestinationStorage();

        ArrayList<String> newList = new ArrayList<>();
        ArrayList<String> proposals = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("fahrziel")) {
            for (Destination dest : destinationStorage.getDestinations()) {
                if (!sender.hasPermission("ctdestinations.see.private") && !dest.isPublic())
                    continue;

                proposals.add(dest.getName());
            }
        }

        else if (cmd.getName().equalsIgnoreCase("fahrzieledit") || cmd.getName().equalsIgnoreCase("fze")) {
            if (args.length == 1) {
                if (sender.hasPermission("ctdestinations.edit.info"))
                    proposals.add("info");
                if (sender.hasPermission("ctdestinations.edit.add"))
                    proposals.add("add");
                if (sender.hasPermission("ctdestinations.edit.remove"))
                    proposals.add("remove");
                if (sender.hasPermission("ctdestinations.edit.setowner"))
                    proposals.add("setowner");
                if (sender.hasPermission("ctdestinations.edit.addmember"))
                    proposals.add("addmember");
                if (sender.hasPermission("ctdestinations.edit.removemember"))
                    proposals.add("removemember");
                if (sender.hasPermission("ctdestinations.edit.setlocation"))
                    proposals.add("setlocation");
                if (sender.hasPermission("ctdestinations.edit.setwarp"))
                    proposals.add("setwarp");
                if (sender.hasPermission("ctdestinations.edit.settype"))
                    proposals.add("settype");
                if (sender.hasPermission("ctdestinations.edit.setpublic"))
                    proposals.add("setpublic");
                if (sender.hasPermission("ctdestinations.edit.setprivate"))
                    proposals.add("setprivate");
                if (sender.hasPermission("ctdestinations.edit.updatemarker"))
                    proposals.add("updatemarker");
                if (sender.hasPermission("ctdestinations.edit.reload"))
                    proposals.add("reload");
                if (sender.hasPermission("ctdestinations.teleport"))
                    proposals.add("tp");
            }

            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add") && sender.hasPermission("ctdestinations.edit.add")) {
                    proposals.add("<name>");
                } else {
                    if (!sender.hasPermission("ctdestinations.edit." + args[0]))
                        return new ArrayList<>();

                    Collection<Destination> destinations = destinationStorage.getDestinations();
                    for (Destination dest : destinationStorage.getDestinations()) {
                        if ((args[0].equalsIgnoreCase("setprivate") && !dest.isPublic()) || (
                                args[0].equalsIgnoreCase("setpublic") && dest.isPublic()))
                            continue;

                        proposals.add(dest.getName());
                    }
                }
            } else if (args.length == 3) {
                if ((args[0].equalsIgnoreCase("setowner") && sender.hasPermission("ctdestinations.edit.setowner")) ||
                        args[0].equalsIgnoreCase("addmember") && sender.hasPermission("ctdestinations.edit.addmember")) {
                    for (Player p : Bukkit.getOnlinePlayers())
                        proposals.add(p.getName());

                } else if (args[0].equalsIgnoreCase("removemember") && sender.hasPermission("ctdestinations.edit.removemember")) {
                    Destination dest = destinationStorage.getDestination(args[1], CraftBahnPlugin.getInstance().getServerName());
                    if (dest != null) {
                        for (UUID uuid : dest.getParticipants()) {
                            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                            if (!participant.hasPlayedBefore()) continue;
                            proposals.add(participant.getName());
                        }
                    }

                } else if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("settype")) && (
                        sender.hasPermission("ctdestinations.edit.add") || sender.hasPermission("ctdestinations.edit.settype"))) {
                    proposals.add("STATION");
                    proposals.add("MAIN_STATION");
                    proposals.add("PUBLIC_STATION");
                    proposals.add("PLAYER_STATION");
                }
            } else if (args.length == 4 && args[0].equalsIgnoreCase("add") &&
                    sender.hasPermission("ctdestinations.edit.add")) {
                proposals.add("PRIVATE");
            }
        }

        if (args.length < 1 || args[args.length - 1].equals("")) {
            newList = proposals;
        } else {
            for (String value : proposals) {
                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    newList.add(value);
            }
        }

        return newList;
    }
}
