package lv.mtm123.pickcharge;

import lv.mtm123.easybar.api.BossBar;
import lv.mtm123.easybar.api.BossBarManager;
import lv.mtm123.easybar.impl.BossBarManagerImpl;
import lv.mtm123.easybar.impl.LegacyBossBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final BossBarManager manager;
    private final Config cfg;

    private final Map<UUID, Integer> players;
    private final Map<UUID, BossBar> bossBars;
    private final String titleTemplate;
    private final String chargedTitleTemplate;

    public PlayerManager(BossBarManager manager, Config cfg) {
        this.manager = manager;
        //this.managera = managera;
        this.cfg = cfg;
        this.players = new HashMap<>();
        this.bossBars = new HashMap<>();
        this.titleTemplate = ChatColor.translateAlternateColorCodes('&', cfg.getBossBarText());
        this.chargedTitleTemplate = ChatColor.translateAlternateColorCodes('&', cfg.getBossBarTextCharged());
    }

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), 0);
            BossBar bar = manager.createBossBar(player, titleTemplate.replace("%charge%", "" + 0),
                    cfg.getBarColor(), cfg.getBarStyle(), 1/(float)cfg.getBlocksRequired());

        bar.setProgress((float)(1/cfg.getBlocksRequired()));
        bar.setVisible(false);
        bossBars.put(player.getUniqueId(), bar);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        BossBar bar = bossBars.remove(player.getUniqueId());
        try {
            bar.removePlayer(player);
        }catch(NullPointerException e){}
    }

    public void setBlocksBroken(Player player, int blocks) {
        players.put(player.getUniqueId(), blocks);
        try {
            BossBar bar = bossBars.get(player.getUniqueId());
            updateBossBar(bar, blocks);
        }catch(Exception e){}
    }

    public void addBlocksBroken(Player player) {

        int add = players.getOrDefault(player.getUniqueId(), 0) + 1;
        if (add > cfg.getBlocksRequired()) {
            return;
        }

        players.put(player.getUniqueId(), add);
        try {
            updateBossBar(bossBars.get(player.getUniqueId()), add);
        }catch(Exception e){}

    }

    public float getBlocksBrokenPercentage(Player player) {
        return (players.getOrDefault(player.getUniqueId(), 0) / (float) cfg.getBlocksRequired()) * 100;
    }

    public void showBossBar(Player player) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar == null) {
            return;
        }

        bar.setVisible(true);
    }

    public void hideBossBar(Player player) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar == null) {
            return;
        }

        bar.setVisible(false);
    }

    private void updateBossBar(BossBar bar, int blocks) {
        if (bar == null) {
            return;
        }

        float progress = blocks / (float) cfg.getBlocksRequired();
        if (progress == 0){
            bar.setProgress(1/(float)cfg.getBlocksRequired());
        }else
        bar.setProgress(progress);

        int percentage = Math.round(progress * 100);
        if(progress >= 1) {
            bar.setText(chargedTitleTemplate.replace("%charge%", String.format("%d", percentage)));
        } else {
            bar.setText(titleTemplate.replace("%charge%", String.format("%d", percentage)));
        }

    }


}
