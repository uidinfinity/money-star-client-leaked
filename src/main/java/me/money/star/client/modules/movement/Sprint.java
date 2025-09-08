package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.network.SprintCancelEvent;
import me.money.star.util.player.MovementUtil;
import me.money.star.util.traits.Util;
import net.minecraft.entity.effect.StatusEffects;

public class Sprint extends Module {

    public Setting<SprintMode> mode = mode("Mode", SprintMode.RAGE);
    public Sprint() {
        super("Sprint", "Automatically sprints",Category.MOVEMENT,true,false,false);
    }



    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() != Stage.PRE) {
            return;
        }
        if (MovementUtil.isInputtingMovement()
                && !Util.mc.player.isSneaking()
                && !Util.mc.player.isRiding()
                && !Util.mc.player.isTouchingWater()
                && !Util.mc.player.isInLava()
                && !Util.mc.player.isHoldingOntoLadder()
                && !Util.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && Util.mc.player.getHungerManager().getFoodLevel() > 6.0F) {
            switch (mode.getValue()) {
                case LEGIT -> {
                    if (Util.mc.player.input.hasForwardMovement()
                            && (!Util.mc.player.horizontalCollision
                            || Util.mc.player.collidedSoftly)) {
                        Util.mc.player.setSprinting(true);
                    }
                }
                case RAGE -> Util.mc.player.setSprinting(true);
            }
        }
    }

    @Subscribe
    public void onSprintCancel(SprintCancelEvent event) {
        if (MovementUtil.isInputtingMovement()
                && !Util.mc.player.isSneaking()
                && !Util.mc.player.isRiding()
                && !Util.mc.player.isTouchingWater()
                && !Util.mc.player.isInLava()
                && !Util.mc.player.isHoldingOntoLadder()
                && !Util.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && Util.mc.player.getHungerManager().getFoodLevel() > 6.0F
                && mode.getValue() == SprintMode.RAGE) {
            event.cancel();
        }
    }

    public enum SprintMode {
        LEGIT,
        RAGE
    }
}
