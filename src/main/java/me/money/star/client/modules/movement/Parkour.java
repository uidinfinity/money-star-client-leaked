package me.money.star.client.modules.movement;

import me.money.star.client.gui.modules.Module;
import me.money.star.util.traits.Util;

public class Parkour extends Module {
    public Parkour(){
        super("Parkour","",Category.MOVEMENT,true,false, false);
    }
    private boolean jumping = false;

    @Override
    public void onUpdate() {
        if (Util.mc.player == null || Util.mc.world == null) return;

        if (Util.mc.player.isOnGround() && !Util.mc.player.isSneaking() && Util.mc.world.isSpaceEmpty(Util.mc.player.getBoundingBox().offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001))) {
            Util.mc.options.jumpKey.setPressed(true);
            jumping = true;
        } else if (jumping) {
            jumping = false;
            Util.mc.options.jumpKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
        if (jumping) {
            Util.mc.options.jumpKey.setPressed(false);
            jumping = false;
        }
    }
}
