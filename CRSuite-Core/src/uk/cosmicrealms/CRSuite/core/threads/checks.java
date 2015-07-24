package uk.cosmicrealms.CRSuite.core.threads;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import uk.cosmicrealms.CRSuite.core.CRSuite;

/**
 * Created by Thomas on 24/07/2015.
 */
public class checks extends BukkitRunnable {
    private CRSuite plugin;

    public checks(CRSuite plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // GameState Check
        if(plugin.serverMOTD.equalsIgnoreCase(Bukkit.getMotd())) {
            plugin.updateGameState();
        }
    }
}
