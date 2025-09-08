package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.SkyboxEvent;

import java.awt.*;

public class SkyColors extends Module {
    public SkyColors() {
        super("SkyColors", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
    }
}
