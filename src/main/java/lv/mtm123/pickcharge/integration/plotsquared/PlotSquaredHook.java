package lv.mtm123.pickcharge.integration.plotsquared;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlotSquaredHook {

    private final PlotAPI api;

    public PlotSquaredHook() {
        this.api = new PlotAPI();
    }

    public boolean canDestroy(Player player, Location loc) {
        if (player == null) {
            return false;
        }

        Plot plot = api.getPlot(loc);
        return !api.isPlotWorld(loc.getWorld()) || plot == null || (plot.hasOwner() && plot.getOwners().contains(player.getUniqueId()));
    }

    public boolean isRoad(Location loc) {
        if (!api.isPlotWorld(loc.getWorld())) {
            return false;
        }

        return BukkitUtil.getLocation(loc).isPlotRoad();
    }

}


