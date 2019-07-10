package lv.mtm123.pickcharge.integration.prisonmines;

import net.lightshard.prisonmines.MineAPI;
import net.lightshard.prisonmines.PrisonMines;
import net.lightshard.prisonmines.mine.Mine;
import org.bukkit.block.Block;

public class PrisonMinesHook {

    private final MineAPI prisonminesAPI;

    public PrisonMinesHook() {
        prisonminesAPI = PrisonMines.getAPI();
    }

    public void onBlockBreak(Block block) {
        Mine mine = prisonminesAPI.getByLocation(block.getLocation());
        if (mine == null) {
            return;
        }

        mine.getPercentageReset().onBlockBreak();
    }

}
