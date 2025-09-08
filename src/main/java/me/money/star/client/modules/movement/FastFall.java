package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.System;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.player.PlayerMoveEvent;
import me.money.star.event.impl.network.TickMovementEvent;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.traits.Util;
import net.minecraft.util.math.Box;

public class FastFall extends Module {

    //
    public Setting<FallMode> fallMode = mode("Mode", FallMode.STEP);
    public Setting<Float> height = num("Height", 1.0f, 3.0f, 10.0f);
    public Setting<Integer> shiftTicks = num("ShiftTicks", 3, 1, 5);

    //
    private boolean prevOnGround;
    //
    private boolean cancelFallMovement;
    private int fallTicks;
    private final Timer fallTimer = new CacheTimer();

    /**
     *
     */
    public FastFall() {
        super("FastFall", "Falls down blocks faster", Category.MOVEMENT,true,false,false);
    }

    @Override
    public void onDisable() {
        cancelFallMovement = false;
        fallTicks = 0;
    }

    @Override public void onUpdate() {
        if(fallMode.getValue()== FallMode.VANILLA) {
            if (System.nullCheck()) return;
            if (Util.mc.player.isInLava() || Util.mc.player.isTouchingWater() || !Util.mc.player.isOnGround()) return;
            Util.mc.player.addVelocity(0, -1, 0);
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE) {
            prevOnGround = Util.mc.player.isOnGround();
            if (fallMode.getValue() == FallMode.STEP) {
                if (Util.mc.player.isRiding()
                        || Util.mc.player.isGliding()
                        || Util.mc.player.isHoldingOntoLadder()
                        || Util.mc.player.isInLava()
                        || Util.mc.player.isTouchingWater()
                        || Util.mc.player.input.playerInput.jump()
                        || Util.mc.player.input.playerInput.sneak()) {
                    return;
                }
                if (//Modules.SPEED.isEnabled() ||
                        MoneyStar.moduleManager.getModuleByClass(LongJump.class).isEnabled()
                //        || Modules.FLIGHT.isEnabled()
                ) {
                    return;
                }
                if (Util.mc.player.isOnGround() && isNearestBlockWithinHeight(height.getValue())) {
                    MoneyStar.movementManager.setMotionY(-3.0);
                    // Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
                }
            }
        }
    }

    @Subscribe
    public void onTickMovement(TickMovementEvent event) {
        if (fallMode.getValue() == FallMode.SHIFT) {
            if (Util.mc.player.isRiding()
                    || Util.mc.player.isGliding()
                    || Util.mc.player.isHoldingOntoLadder()
                    || Util.mc.player.isInLava()
                    || Util.mc.player.isTouchingWater()
                    || Util.mc.player.input.playerInput.jump()
                    || Util.mc.player.input.playerInput.sneak()) {
                return;
            }
            if (//Modules.SPEED.isEnabled() ||
                    MoneyStar.moduleManager.getModuleByClass(LongJump.class).isEnabled()
                //        || Modules.FLIGHT.isEnabled()
            ) {
                return;
            }
            if (Util.mc.player.getVelocity().y < 0 && prevOnGround && !Util.mc.player.isOnGround()
                    && isNearestBlockWithinHeight(height.getValue() + 0.01)) {
                fallTimer.reset();
                event.cancel();
                event.setIterations(shiftTicks.getValue());
                cancelFallMovement = true;
                fallTicks = 0;
            }
        }
    }

    @Subscribe
    public void onPlayerMove(PlayerMoveEvent event) {

        if (cancelFallMovement && fallMode.getValue() == FallMode.SHIFT) {
            event.setX(0.0);
            event.setZ(0.0);
            MoneyStar.movementManager.setMotionXZ(0.0, 0.0);
            ++fallTicks;
            if (fallTicks > shiftTicks.getValue()) {
                cancelFallMovement = false;
                fallTicks = 0;
            }
        }
    }

    private boolean isNearestBlockWithinHeight(double height) {
        Box bb = Util.mc.player.getBoundingBox();
        for (double i = 0; i < height + 0.5; i += 0.01) {
            if (!Util.mc.world.isSpaceEmpty(Util.mc.player, bb.offset(0, -i, 0))) {
                return true;
            }
        }
        return false;
    }

    public enum FallMode {
        STEP,
        SHIFT,
        VANILLA
    }
}
