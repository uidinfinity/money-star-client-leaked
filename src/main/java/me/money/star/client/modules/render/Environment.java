package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.RenderFogEvent;
import me.money.star.event.impl.SkyboxEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.network.PacketEvent;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.*;

public class Environment extends Module {
    public Setting<Boolean> timeChange  = bool("Time", false);
    public Setting<Integer> time = num("Time",  200, -200, 200);
    public Environment() {
        super("Environment", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
    }


}
