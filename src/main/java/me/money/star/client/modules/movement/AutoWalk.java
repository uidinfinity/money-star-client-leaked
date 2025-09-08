package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.util.traits.Util;


public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", "Automatically moves forward", Category.MOVEMENT,true,false,false);
    }
    public Setting<Boolean> lock = bool("Lock", false);
    @Override
    public void onDisable() {
        Util.mc.options.forwardKey.setPressed(false);
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE) {
            Util.mc.options.forwardKey.setPressed(!Util.mc.options.sneakKey.isPressed()
                    && (!lock.getValue() || (!Util.mc.options.jumpKey.isPressed() && Util.mc.player.isOnGround())));
        }
    }
}
