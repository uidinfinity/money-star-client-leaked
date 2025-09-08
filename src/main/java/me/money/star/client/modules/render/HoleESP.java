package me.money.star.client.modules.render;

import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.modules.combat.LegacyCrystal;
import me.money.star.client.settings.Setting;

import java.awt.*;

public class HoleESP extends Module {
    private static HoleESP INSTANCE;
    public Setting<Boolean> itemGlobal  = bool("Global", false);
    public Setting<Float> range = num("Range", 5.0f, 3.0f, 25.0f);
    public HoleESP() {
        super("HoleESP", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
        INSTANCE = this;
    }
    public static HoleESP getInstance()
    {
        return INSTANCE;
    }
    public double getRange()
    {
        return range.getValue();
    }

}
