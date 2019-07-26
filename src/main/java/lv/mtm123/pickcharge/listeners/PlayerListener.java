package lv.mtm123.pickcharge.listeners;

import lv.mtm123.pickcharge.Config;
import lv.mtm123.pickcharge.PickCharge;
import lv.mtm123.pickcharge.PlayerManager;
import lv.mtm123.pickcharge.integration.prisonmines.PrisonMinesHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class PlayerListener implements Listener {

    private final PickCharge plugin;
    private final PrisonMinesHook prisonMinesHook;
    private final PlayerManager playerManager;
    private final Config cfg;

    private static final Set<Material> TOOLS = EnumSet.of(Material.WOOD_PICKAXE,
            Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE);

    private static Field durabilityField;

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


    @EventHandler(priority = EventPriority.HIGHEST)
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

        if (!plugin.chargingAllowed(event.getPlayer(), event.getPlayer().getLocation())) {
            return;
        }

        Player player = event.getPlayer();
        World w = event.getPlayer().getWorld();

        float power = cfg.getExplosionRadius();

        Location center = player.getLocation();

        Set<Block> blocks = simulateExplosion(center, cfg.getExplosionRadius());
        blocks.stream().filter(b -> plugin.canBreakBlock(player, b.getLocation()))
                .forEach(b -> {
                    for (int i = 0;i<=cfg.getDropMultiplier();i++) {
                        b.getDrops().forEach(dr -> player.getInventory().addItem(dr));
                    }
                    b.setType(Material.AIR);
                    if (prisonMinesHook != null) {
                        prisonMinesHook.onBlockBreak(b);
                    }

                    spawnParticles(center, b.getLocation(), cfg.getExplosionRadius());
                });

        w.playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

        Particle p;
        if (power >= 2) {
            p = Particle.EXPLOSION_HUGE;
        } else {
            p = Particle.EXPLOSION_LARGE;
        }

        w.spawnParticle(p, center.getX(), center.getY(), center.getZ(), 1, 0.0, 0.0, 0.0, 1.0);
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

        //Taken from NMS

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
        1, d3, d4, d5, 1);

        w.spawnParticle(Particle.EXPLOSION_NORMAL, d0, d1, d2,1, d3, d4, d5, 1);

    }

    private Set<Block> simulateExplosion(Location loc, float power) {

        //Taken from NMS
        World world = loc.getWorld();
        double posX = loc.getX();
        double posY = loc.getY();
        double posZ = loc.getZ();
        Set<Block> affectedBlocks = new HashSet<>();
        if (power < 0.1F) {
            return affectedBlocks;
        } else {
            for(int k = 0; k < 16; ++k) {
                for(int i = 0; i < 16; ++i) {
                    for(int j = 0; j < 16; ++j) {
                        if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                            double d0 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                            double d1 = (double)((float)i / 15.0F * 2.0F - 1.0F);
                            double d2 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 /= d3;
                            d1 /= d3;
                            d2 /= d3;
                            float f = power * (0.7F + ThreadLocalRandom.current().nextFloat() * 0.6F);
                            double d4 = posX;
                            double d5 = posY;

                            for(double d6 = posZ; f > 0.0F; f -= 0.22500001F) {
                                Block blockposition = world.getBlockAt(new Location(world, d4, d5, d6));
                                Material mat = blockposition.getType();
                                if (mat != Material.AIR) {
                                    float f2 = getBlockDurability(blockposition) / 5.0F;
                                    f -= (f2 + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && blockposition.getY() < 256 && blockposition.getY() >= 0) {
                                    affectedBlocks.add(blockposition);
                                }

                                d4 += d0 * 0.30000001192092896D;
                                d5 += d1 * 0.30000001192092896D;
                                d6 += d2 * 0.30000001192092896D;
                            }
                        }
                    }
                }
            }

            return affectedBlocks;
        }
    }

    private float getBlockDurability(Block block) {

        if (durabilityField == null) {
            try {
                durabilityField = net.minecraft.server.v1_12_R1.Block.class.getDeclaredField("durability");
                durabilityField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return 3;
            }
        }

        try {
            return (float) durabilityField.get(CraftMagicNumbers.getBlock(block));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return 3;
    }

}
