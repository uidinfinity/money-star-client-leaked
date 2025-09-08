package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.MovementUtil;
import me.money.star.util.traits.Util;


public class Flight extends Module {


    public Setting<FlightMode> mode = mode("Mode", FlightMode.NORMAL);
    public Setting<Float> hspeed = num("Speed ", 2.5f, 0.1f, 10.0f);
    public Setting<Float> vspeed = num("VerticalSpeed ", 2.5f, 0.1f, 10.0f);
    public Setting<Boolean> antiKick = bool("AntiKick", true);
    public Setting<Boolean> accelerate = bool("Accelerate", false);
    public Setting<Float> accelerateSpeed = num("AccelerateSpeed ", 0.2f, 0.1f, 1.0f);
    public Setting<Float> maxSpeed = num("MaxSpeed ", 5.0f, 1.0f, 10.0f);


    private double speed;
    private final Timer antiKickTimer = new CacheTimer();
    private final Timer antiKick2Timer = new CacheTimer();

    public Flight() {
        super("Flight", "Allows the player to fly in survival", Category.MOVEMENT,true,false,false);
    }



    @Override
    public void onEnable() {
        antiKickTimer.reset();
        antiKick2Timer.reset();
        if (mode.getValue() == FlightMode.VANILLA) {
            enableVanillaFly();
        }
        speed = 0.0;
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == FlightMode.VANILLA) {
            disableVanillaFly();
        }
    }

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event) {
        if (accelerate.getValue()) {
            if (!MovementUtil.isInputtingMovement() || Util.mc.player.horizontalCollision) {
                speed = 0.0f;
            }
            speed += accelerateSpeed.getValue();
            if (speed > maxSpeed.getValue()) {
                speed = maxSpeed.getValue();
            }
        }
        else {
            speed = hspeed.getValue();
        }
        if (mode.getValue().equals(FlightMode.VANILLA)) {
            Util.mc.player.getAbilities().setFlySpeed((float) (speed * 0.05f));
        }
        else {
            Util.mc.player.getAbilities().setFlySpeed(0.05f);
        }
        // Vanilla fly kick checks every 80 ticks
        if (antiKickTimer.passed(3900) && antiKick.getValue()) {
            MoneyStar.movementManager.setMotionY(-0.04);
            antiKickTimer.reset();
        } else if (antiKick2Timer.passed(4000) && antiKick.getValue()) {
            MoneyStar.movementManager.setMotionY(0.04);
            antiKick2Timer.reset();
        } else if (mode.getValue() == FlightMode.NORMAL) {
            MoneyStar.movementManager.setMotionY(0.0);
            if (Util.mc.options.jumpKey.isPressed()) {
                MoneyStar.movementManager.setMotionY(vspeed.getValue());
            } else if (Util.mc.options.sneakKey.isPressed()) {
                MoneyStar.movementManager.setMotionY(-vspeed.getValue());
            }
        }
        if (mode.getValue() == FlightMode.NORMAL) {
            speed = Math.max(speed, 0.2873f);
            float forward = Util.mc.player.input.movementForward;
            float strafe = Util.mc.player.input.movementSideways;
            float yaw = Util.mc.player.getYaw();
            if (forward == 0.0f && strafe == 0.0f) {
                MoneyStar.movementManager.setMotionXZ(0.0f, 0.0f);
                return;
            }
            double rx = Math.cos(Math.toRadians(yaw + 90.0f));
            double rz = Math.sin(Math.toRadians(yaw + 90.0f));
            MoneyStar.movementManager.setMotionXZ((forward * speed * rx) + (strafe * speed * rz),
                    (forward * speed * rz) - (strafe * speed * rx));
        }
    }



    private void enableVanillaFly() {
        Util.mc.player.getAbilities().allowFlying = true;
        Util.mc.player.getAbilities().flying = true;
    }

    private void disableVanillaFly() {
        if (!Util.mc.player.isCreative()) {
            Util.mc.player.getAbilities().allowFlying = false;
        }
        Util.mc.player.getAbilities().flying = false;
        Util.mc.player.getAbilities().setFlySpeed(0.05f);
    }

    public enum FlightMode {
        NORMAL,
        VANILLA
    }
}
