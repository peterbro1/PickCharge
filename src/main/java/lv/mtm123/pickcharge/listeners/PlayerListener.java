package lv.mtm123.pickcharge.listeners;

import lv.mtm123.pickcharge.Config;
import lv.mtm123.pickcharge.PickCharge;
import lv.mtm123.pickcharge.PlayerManager;
import lv.mtm123.pickcharge.integration.prisonmines.PrisonMinesHook;
import lv.mtm123.spigotutils.WorldUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {

    private final PickCharge plugin;
    private final PrisonMinesHook prisonMinesHook;
    private final PlayerManager playerManager;
    private final Config cfg;

    private static final Set<Material> TOOLS = EnumSet.of(Material.WOOD_PICKAXE,
            Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE);

    public PlayerListener(PickCharge plugin, PrisonMinesHook prisonMinesHook, PlayerManager playerManager, Config cfg) {
        this.plugin = plugin;
        this.prisonMinesHook = prisonMinesHook;
        this.playerManager = playerManager;
        this.cfg = cfg;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!plugin.chargingAllowed(event.getPlayer(), event.getBlock().getLocation())) {
            return;
        }

        playerManager.addBlocksBroken(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null || !TOOLS.contains(event.getMaterial())) {
            return;
        }

        if (playerManager.getBlocksBrokenPercentage(event.getPlayer()) != 100) {
            return;
        }

        Player player = event.getPlayer();
        World w = event.getPlayer().getWorld();

        float power = cfg.getExplosionRadius();

        Location center = player.getLocation();

        Set<Block> blocks = WorldUtil.generateExplosion(center, cfg.getExplosionRadius());
        blocks.stream().filter(b -> plugin.canBreakBlock(player, b.getLocation()))
                .forEach(b -> {
                    b.getDrops().forEach(dr -> w.dropItemNaturally(b.getLocation(), dr));
                    b.setType(Material.AIR);

                    if (prisonMinesHook != null) {
                        prisonMinesHook.onBlockBreak(b);
                    }
                });

        w.playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

        Particle p;
        if (power >= 2) {
            p = Particle.EXPLOSION_HUGE;
        } else {
            p = Particle.EXPLOSION_LARGE;
        }

        w.spawnParticle(p, center.getX(), center.getY(), center.getZ(), 1, 1.0, 0.0, 0.0);
        playerManager.setBlocksBroken(player, 0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        playerManager.removePlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerManager.addPlayer(player);

        if (plugin.chargingAllowed(player, player.getLocation())) {
            playerManager.showBossBar(player);
        }
    }

    private void spawnParticles(Location center, Location current, float power) {

        Random rnd = ThreadLocalRandom.current();

        double d0 = (double) ((float) current.getX() + rnd.nextFloat());
        double d1 = (double) ((float) current.getY() + rnd.nextFloat());
        double d2 = (double) ((float) current.getZ() + rnd.nextFloat());
        double d3 = d0 - center.getX();
        double d4 = d1 - center.getY();
        double d5 = d2 - center.getZ();
        double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

        d3 /= d6;
        d4 /= d6;
        d5 /= d6;
        double d7 = 0.5D / (d6 / (double) power + 0.1D);

        d7 *= (double) (rnd.nextFloat() * rnd.nextFloat() + 0.3F);
        d3 *= d7;
        d4 *= d7;
        d5 *= d7;

        World w = center.getWorld();

        w.spawnParticle(Particle.SMOKE_NORMAL, (d0 + center.getX()) / 2.0D,
                (d1 + center.getY()) / 2.0D,
                (d2 + center.getZ()) / 2.0D,
        1, d3, d4, d5);

        w.spawnParticle(Particle.EXPLOSION_NORMAL, d0, d1, d2,1, d3, d4, d5);

    }

}
