package uk.cosmicrealms.CRSuite.core;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.sql.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Thomas on 24/07/2015.
 */
public class CRSuite extends JavaPlugin implements PluginMessageListener {
    // This is the Main Connector Plugin for CRSuite, and will be used for all Communications between Plugins
    FileConfiguration config = this.getConfig(); // Configuration
    Connection conn; // Database Connection
    String serverName = config.getString("server.name");
    public static String serverMOTD = Bukkit.getMotd();
    String serverList[];
    public static byte[] recentMessage;
    public static String version = "0.1";

    // Logging for all Plugins Handled by This.
    public void log(String message) {
        Logger logger = getLogger();
        logger.log(Level.INFO, message);
    }

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        this.saveDefaultConfig();
        connectToDatabase();
        this.getCommand("CRSuite-info").setExecutor(new CRSuiteCommand(this));

    }

    public void onDisable() {
        try {
            if (conn.isClosed()) {
                connectToDatabase();
            }
            if(serverInDatabase(getServerName())) {
                PreparedStatement stateUpdate = conn.prepareStatement("UPDATE `GameStates` SET `State`=? WHERE `Server`=?");
                stateUpdate.setString(1, "Offline");
                stateUpdate.setString(2, getServerName());
                stateUpdate.executeUpdate();
                stateUpdate.close();

            }
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Opening & Closing of the Database

    private void connectToDatabase() {
        String url = config.getString("database.url");
        String username = config.getString("database.username");
        String password = config.getString("database.password");
        String port = config.getString("database.port");
        String database = config.getString("database.database");
        String DB_NAME = "jdbc:mysql://"+url+":"+port+"/"+database;
        try {
            log("About to connect to Database!");
            //conn = DriverManager.getConnection(DB_NAME, username, password);
            conn = DriverManager.getConnection("jdbc:mysql://panel.cosmicrealms.uk:3306/CR_GameMonitor", "TestUserAccount", "hFVwCJYNFTduK9zC");
            log("Successfully connected");

            log("About to Create a Statement");
        }catch(Exception e){
            log("Failed to Connect to the Database!");
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Player in Database Checks

    public Boolean playerInDatabase(Player player) {
        boolean playerFound = false;
        try {
            if(conn.isClosed()) {
                connectToDatabase();
            }
            PreparedStatement sql = conn
                    .prepareStatement("SELECT * FROM `player_movement` WHERE Player=?");
            sql.setString(1, player.getName());

            ResultSet resultSet = sql.executeQuery();
            playerFound = resultSet.next();
            sql.close();
            resultSet.close();
        }catch(Exception e) {
            e.printStackTrace();
            //}finally {
            //    closeConnection();
        }
        return playerFound;
    }

    // Get & Set Player Movements

    public String getPlayerMovement(Player player) {
        try {
            if (conn.isClosed()) {
                connectToDatabase();
            }
            PreparedStatement sql = conn
                    .prepareStatement("SELECT * FROM `player_movement` WHERE Player=?");
            sql.setString(1, player.getName());

            ResultSet resultSet = sql.executeQuery();
            resultSet.next();
            return resultSet.getString("Movement");
        } catch (Exception e) {
            e.printStackTrace();
            return "NoRoute";
        }
    }

    public void setPlayerMovement(Player player, String movement) {
        try {
            if (playerInDatabase(player)) {
                PreparedStatement stateUpdate = conn.prepareStatement("UPDATE `player_movement` SET `Movement`=? WHERE `Player`=?");
                stateUpdate.setString(1, movement);
                stateUpdate.setString(2, player.getName());
                stateUpdate.executeUpdate();
                stateUpdate.close();

            } else {
                log("Attempting to Insert");
                PreparedStatement newServer = conn.prepareStatement("INSERT INTO `player_movement` values(?,?)");
                newServer.setString(1,player.getName());
                newServer.setString(2,movement);
                newServer.execute();
                newServer.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Local Server Handlers

    public boolean serverInDatabase(String serverName) {
        boolean serverFound = false;
        try {
            if(conn.isClosed()) {
                connectToDatabase();
            }
            PreparedStatement sql = conn
                    .prepareStatement("SELECT * FROM `GameStates` WHERE Server=?");
            sql.setString(1, serverName);

            ResultSet resultSet = sql.executeQuery();
            serverFound = resultSet.next();
            sql.close();
            resultSet.close();
        }catch(Exception e) {
            e.printStackTrace();
            //}finally {
            //    closeConnection();
        }
        return serverFound;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerRoute(String route) {
        log("Setting Server Route to" + route);

    }

    public void updateGameState() {
        log("Preparing to Update Database!");
        try {
            if (serverInDatabase(serverName)) {
                log("ServerMOTD: " + serverMOTD);
                PreparedStatement stateUpdate = conn.prepareStatement("UPDATE `GameStates` SET `State`=? WHERE `Server`=?");
                stateUpdate.setString(1, serverMOTD);
                stateUpdate.setString(2, getServerName());
                stateUpdate.executeUpdate();
                stateUpdate.close();

            } else {
                PreparedStatement newServer = conn.prepareStatement("INSERT INTO `GameStates` values(?,?,?)");
                newServer.setString(1, getServerName());
                newServer.setString(2, serverMOTD);
                newServer.setString(3, getConfig().getString("Route"));
                newServer.execute();
                newServer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Remote Server Handlers

    public String getRemoteServerInfo(String type, String server) {
        if (type.equalsIgnoreCase("serverState")) {
            if ((serverInDatabase(server))) {
                try {
                    if (conn.isClosed()) {
                        connectToDatabase();
                    }
                    PreparedStatement sql = conn
                            .prepareStatement("SELECT * FROM `GameStates` WHERE Server=?");
                    sql.setString(1, server);

                    ResultSet resultSet = sql.executeQuery();
                    resultSet.next();
                    String serverState = resultSet.getString("State");


                    sql.close();
                    resultSet.close();
                    return serverState;
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error";
                }
            } else {
                return "Server Not Found";
            }
        }
        return "Cannot Get Server Data";
    }


    // Plugin Messages


    public void connectPluginMessage(String server, Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void getServersPluginMessage(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServers");
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        recentMessage = message;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServers")) {
            serverList = in.readUTF().split(", ");
        }
    }
}
