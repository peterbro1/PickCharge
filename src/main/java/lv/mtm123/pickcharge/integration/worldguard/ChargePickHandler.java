package lv.mtm123.pickcharge.integration.worldguard;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import lv.mtm123.pickcharge.PickCharge;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ChargePickHandler extends FlagValueChangeHandler<State> {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<ChargePickHandler> {
        @Override
        public ChargePickHandler create(Session session) {
            return new ChargePickHandler(session);
        }
    }

    protected ChargePickHandler(Session session) {
        super(session, PickCharge.CHARGE_PICKAXE);
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet applicableRegionSet, State state) {

        if (state != State.ALLOW) {
            return;
        }

        PickCharge.getPlayerManager().addPlayer(player);
    }

    @Override
    protected boolean onSetValue(Player player, Location location, Location location1, ApplicableRegionSet applicableRegionSet, State state, State t1, MoveType moveType) {

        if (state != State.ALLOW) {
            return true;
        }

        PickCharge.getPlayerManager().showBossBar(player);
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location location, Location location1, ApplicableRegionSet applicableRegionSet, State state, MoveType moveType) {

        if (state != State.ALLOW) {
            return true;
        }

        PickCharge.getPlayerManager().hideBossBar(player);
        return true;
    }

}
