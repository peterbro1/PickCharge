package lv.mtm123.pickcharge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, Integer> players;
    private final Map<UUID, BossBar> bossBars;
    private final Config cfg;
    private final String titleTemplate;
    private final String chargedTitleTemplate;

    public PlayerManager(Config cfg) {
        this.players = new HashMap<>();
        this.bossBars = new HashMap<>();
        this.cfg = cfg;
        this.titleTemplate = ChatColor.translateAlternateColorCodes('&', cfg.getBossBarText());
        this.chargedTitleTemplate = ChatColor.translateAlternateColorCodes('&', cfg.getBossBarTextCharged());
    }

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), 0);

        BossBar bar = Bukkit.createBossBar(titleTemplate.replace("%charge%", "" + 0), cfg.getBarColor(), cfg.getBarStyle());
        bar.setVisible(false);
        bar.addPlayer(player);
        bar.setProgress(0);
        bossBars.put(player.getUniqueId(), bar);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        bossBars.remove(player.getUniqueId());
    }

    public void setBlocksBroken(Player player, int blocks) {
        players.put(player.getUniqueId(), blocks);

        BossBar bar = bossBars.get(player.getUniqueId());
        updateBossBar(bar, blocks);
    }

    public void addBlocksBroken(Player player) {

        int add = players.getOrDefault(player.getUniqueId(), 0) + 1;
        if (add > cfg.getBlocksRequired()) {
            return;
        }

        players.put(player.getUniqueId(), add);
        updateBossBar(bossBars.get(player.getUniqueId()), add);

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

        double progress = blocks / (double) cfg.getBlocksRequired();
        bar.setProgress(progress);

        int percentage = (int) Math.round(progress * 100);
        if(progress >= 1) {
            bar.setTitle(chargedTitleTemplate.replace("%charge%", String.format("%d", percentage)));
        } else {
            bar.setTitle(titleTemplate.replace("%charge%", String.format("%d", percentage)));
        }

    }

}
