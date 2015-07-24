package uk.cosmicrealms.CRSuite.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

/**
 * Created by Thomas on 24/07/2015.
 */
public class CRSuiteCommand implements CommandExecutor {
    private final CRSuite plugin;

    public CRSuiteCommand(CRSuite plugin) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("CRSuite-info")) {
            sender.sendMessage("CRSuite-Core Version"+ plugin.version);
            return true;
        }
        return false;
    }
}
