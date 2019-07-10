package lv.mtm123.pickcharge;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

@ConfigSerializable
public class Config {

    @Setting("explosion-radius")
    private float explosionRadius = 4.0f;

    @Setting("boss-bar-text")
    private String bossBarText = "Current charge: %charge%/100%";

    @Setting("boss-bar-text-charged")
    private String bossBarTextCharged = "Right-click to release charge!";

    @Setting("boss-bar-color")
    private BarColor barColor = BarColor.GREEN;

    @Setting("boss-bar-style")
    private BarStyle barStyle = BarStyle.SOLID;

    @Setting("blocks-required")
    private int blocksRequired = 10;

    public float getExplosionRadius() {
        return explosionRadius;
    }

    public String getBossBarText() {
        return bossBarText;
    }

    public int getBlocksRequired() {
        return blocksRequired;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public String getBossBarTextCharged() {
        return bossBarTextCharged;
    }

}
