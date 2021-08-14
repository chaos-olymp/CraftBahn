package de.crafttogether.craftbahn;

import de.crafttogether.craftbahn.commands.Commands;
import de.crafttogether.craftbahn.commands.ListCommand;
import de.crafttogether.craftbahn.commands.MobEnterCommand;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.listener.TrainEnterListener;
import de.crafttogether.craftbahn.util.MarkerManager;
import de.crafttogether.craftbahn.util.MySQLAdapter;
import de.crafttogether.craftbahn.util.MySQLAdapter.MySQLConfig;
import de.crafttogether.craftbahn.util.MySQLAdapter.MySQLConnection;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class CraftBahn extends JavaPlugin {
    private static CraftBahn plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private FileConfiguration config;
    private MySQLAdapter MySQLAdapter;

    @Override
    public void onEnable() {
        plugin = this;

        // Check dependencies
        if (!getServer().getPluginManager().isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find BKCommonLib");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find TrainCarts");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("dynmap")) {
            plugin.getLogger().warning("Couldn't find Dynmap");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Create default config
        saveDefaultConfig();

        // Initialize
        config = getConfig();
        dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("Dynmap");
        serverName = config.getString("Settings.ServerName");

        // Register Listener
        getServer().getPluginManager().registerEvents(new TrainEnterListener(), this);
        //getServer().getPluginManager().registerEvents(new PlayerSpawnListener(), this);

        // Register Commands
        Commands commands = new Commands();
        registerCommand("rbf", commands);
        registerCommand("fahrziel", commands);
        registerCommand("fahrziele", new ListCommand());
        registerCommand("mobenter", new MobEnterCommand());
        registerCommand("setroute", new ListCommand());
        registerCommand("setdestination", new ListCommand());
        registerCommand("fahrzieledit", commands);
        registerCommand("fze", commands);

        // Register SignActions (TrainCarts)
        TCHelper.registerActionSigns();

        // Setup MySQLConfig
        MySQLAdapter.MySQLConfig myCfg = new MySQLConfig();
        myCfg.setHost(config.getString("MySQL.Host"));
        myCfg.setPort(config.getInt("MySQL.Port"));
        myCfg.setUsername(config.getString("MySQL.Username"));
        myCfg.setPassword(config.getString("MySQL.Password"));
        myCfg.setDatabase(config.getString("MySQL.Database"));
        myCfg.setTablePrefix(config.getString("MySQL.TablePrefix"));

        if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize MySQLAdapter
        MySQLAdapter = new MySQLAdapter(myCfg);
        MySQLConnection mySQL = MySQLAdapter.getConnection();

        // Create Tables
        try {
            ResultSet result = mySQL.query("SHOW TABLES LIKE '%sdestinations';", mySQL.getTablePrefix());

            if (!result.next()) {
                getLogger().info("[MySQL]: Create Tables ...");

                mySQL.execute(
                    "CREATE TABLE `%sdestinations` (\n" +
                    "  `id` int(11) NOT NULL,\n" +
                    "  `name` varchar(24) NOT NULL,\n" +
                    "  `type` varchar(24) NOT NULL,\n" +
                    "  `server` varchar(24) NOT NULL,\n" +
                    "  `world` varchar(24) NOT NULL,\n" +
                    "  `loc_x` double NOT NULL,\n" +
                    "  `loc_y` double NOT NULL,\n" +
                    "  `loc_z` double NOT NULL,\n" +
                    "  `owner` varchar(36) NOT NULL,\n" +
                    "  `participants` longtext DEFAULT NULL,\n" +
                    "  `public` tinyint(1) NOT NULL,\n" +
                    "  `tp_x` double DEFAULT NULL,\n" +
                    "  `tp_y` double DEFAULT NULL,\n" +
                    "  `tp_z` double DEFAULT NULL\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n"
                , mySQL.getTablePrefix());

                mySQL.execute(
                    "ALTER TABLE `%sdestinations`\n" +
                    "  ADD PRIMARY KEY (`id`);"
                , mySQL.getTablePrefix());

                mySQL.execute(
                    "ALTER TABLE `%sdestinations`\n" +
                    "  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;"
                , mySQL.getTablePrefix());
            }
        }
        catch (SQLException ex) {
            getLogger().warning("[MySQL]: " + ex.getMessage());
        }
        finally {
            mySQL.close();
        }

        Bukkit.getServer().getScheduler().runTask(this, () -> {
            // Load all destinations from database into our cache
            DestinationStorage.loadAll((err, destinations) -> {
                getLogger().info("Loaded " + destinations.size() + " destinations");

                getLogger().info("Setup MarkerSets...");
                MarkerManager.createMarkerSets();
                getLogger().info("Setup Markers...");

                int markersCreated = 0;
                for (Destination dest : destinations) {
                    if (!getServerName().equalsIgnoreCase(dest.getServer()))
                        continue;

                    if(MarkerManager.addMarker(dest, true))
                        markersCreated++;
                }

                getLogger().info("Created " + markersCreated + " markers.");
                getLogger().info("Marker-Setup completed.");
            });
        });
    }

    public void onDisable() {
        // Unregister SignActions (TrainCarts)
        TCHelper.unregisterActionSigns();

        // Shutdown MySQL-Adapter
        if(MySQLAdapter != null)
            MySQLAdapter.disconnect();
    }

    private void registerCommand(String cmd, TabExecutor executor) {
        Objects.requireNonNull(getCommand(cmd)).setExecutor(executor);
        Objects.requireNonNull(getCommand(cmd)).setTabCompleter(executor);
    }

    public MySQLAdapter getMySQLAdapter() { return MySQLAdapter; }
    public DynmapAPI getDynmap() { return dynmap; }
    public String getServerName() { return serverName; }
    public static CraftBahn getInstance() { return plugin; }
}
