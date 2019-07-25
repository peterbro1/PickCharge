package lv.mtm123.pickcharge;

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import lv.mtm123.easybar.EasyBar;
import lv.mtm123.pickcharge.integration.plotsquared.PlotSquaredHook;
import lv.mtm123.pickcharge.integration.prisonmines.PrisonMinesHook;
import lv.mtm123.pickcharge.integration.worldguard.ChargePickHandler;
import lv.mtm123.pickcharge.listeners.PlayerListener;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class PickCharge extends JavaPlugin {

    public static final Flag<StateFlag.State> CHARGE_PICKAXE = new StateFlag("charge-pick", false);
    private static PlayerManager playerManager;

    private WorldGuardPlugin wgPlugin;
    private PlotSquaredHook plotSquaredHook;
    private PrisonMinesHook prisonMinesHook;
    private RegionQuery rq;
    public static ViaAPI api = null;

    @Override
    public void onLoad() {

        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            getLogger().log(Level.SEVERE, "WorldGuard not found! Plugin will not work!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("ViaVersion") == null){
            getLogger().log(Level.SEVERE, "ViaVersion not found! Players on 1.8 will not receive a boss bar!");

        }else
            api = Via.getAPI();

        FlagRegistry registry = WGBukkit.getPlugin().getFlagRegistry();

        try {
            registry.register(CHARGE_PICKAXE);
        } catch (FlagConflictException e) {
            e.printStackTrace();
            getLogger().log(Level.SEVERE, "Conflict while registering flag! Remove the conflicting plugin or contact the developer!");
        }

    }

    @Override
    public void onEnable() {

        Config cfg;
        try {
            cfg = loadConfig();
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
            getLogger().log(Level.SEVERE, "Failed to load config! Plugin will not work!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        wgPlugin = WGBukkit.getPlugin();

        SessionManager sessionManager = wgPlugin.getSessionManager();
        sessionManager.registerHandler(ChargePickHandler.FACTORY, null);

        if (getServer().getPluginManager().getPlugin("PlotSquared") == null) {
            getLogger().log(Level.SEVERE, "PlotSquared not found! Plugin won't check for PlotSquared protection");
            return;
        } else {
            plotSquaredHook = new PlotSquaredHook();
        }

        if (getServer().getPluginManager().getPlugin("PrisonMines") == null) {
            getLogger().log(Level.SEVERE, "PrisonMines not found! PrisonMines hook will not work!");
            return;
        } else {
            prisonMinesHook = new PrisonMinesHook();
        }

        getLogger().log(Level.SEVERE,"Creating palyer");
        playerManager = new PlayerManager(EasyBar.getInstance().getBossBarManager(), cfg);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, prisonMinesHook, playerManager, cfg), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private Config loadConfig() throws ObjectMappingException, IOException {
        File mainDir = getDataFolder().getAbsoluteFile();
        if (!mainDir.exists()) {
            //noinspection ResultOfMethodCallIgnored - Not interested in knowing the result
            mainDir.mkdirs();
        }

        File cfgFile = new File(getDataFolder().getAbsoluteFile(), "config.yml");

        ObjectMapper<Config>.BoundInstance instance = ObjectMapper.forClass(Config.class).bindToNew();
        YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder()
                .setFile(cfgFile).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();

        //Pretty sure I'm doing this part wrong
        SimpleCommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        if (!cfgFile.exists()) {
            instance.serialize(node);
            loader.save(node);
        }

        instance.populate(loader.load());

        return instance.getInstance();

    }

    public static PlayerManager getPlayerManager() {
        return playerManager;
    }

    public boolean canBreakBlock(Player player, Location loc) {
        return chargingAllowed(player, loc) && wgPlugin.canBuild(player, loc)
                && (plotSquaredHook != null &&
                !plotSquaredHook.isRoad(loc)
                && plotSquaredHook.canDestroy(player, loc));

    }

    public boolean chargingAllowed(Player player, Location loc) {
        if (rq == null) {
            rq = wgPlugin.getRegionContainer().createQuery();
        }

        return rq.testState(loc, player, (StateFlag) CHARGE_PICKAXE);
    }

}
