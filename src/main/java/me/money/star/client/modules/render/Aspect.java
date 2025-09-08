package me.money.star.client.modules.render;

import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;

public class Aspect extends Module {

    public Setting<Float> ratio = num("Ratio", 1.78f, 0.0f, 5.0f);

    public Aspect() {
        super("Aspect", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
    }


}
