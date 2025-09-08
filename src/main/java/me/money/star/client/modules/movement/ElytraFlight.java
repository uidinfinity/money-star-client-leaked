package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.entity.player.TravelEvent;
import me.money.star.mixin.accessor.AccessorPlayerMoveC2SPacket;
import me.money.star.util.traits.Util;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
public class ElytraFlight extends Module {

    public Setting<FlyMode> mode = mode("Mode", FlyMode.CONTROL);
    public Setting<Float> speed = num("Speed ", 2.5f, 0.1f, 10.0f);
    public Setting<Float> vspeed = num("VerticalSpeed ", 2.5f, 0.1f, 10.0f);
    private float pitch;


    public ElytraFlight() {
        super("ElytraFlight", "Allows you to fly freely using an elytra", Category.MOVEMENT,true,false,false);
    }



    @Subscribe
    public void onTravel(TravelEvent event) {
        if (event.getStage() != Stage.PRE || Util.mc.player == null
                || Util.mc.world == null || !Util.mc.player.isGliding()) {
            return;
        }
        switch (mode.getValue()) {
            case CONTROL -> {
                event.cancel();
                float forward = Util.mc.player.input.movementForward;
                float strafe = Util.mc.player.input.movementSideways;
                float yaw = Util.mc.player.getYaw();
                if (forward == 0.0f && strafe == 0.0f) {
                    MoneyStar.movementManager.setMotionXZ(0.0, 0.0);
                } else {
                    pitch = 12;
                    double rx = Math.cos(Math.toRadians(yaw + 90.0f));
                    double rz = Math.sin(Math.toRadians(yaw + 90.0f));
                    MoneyStar.movementManager.setMotionXZ(((forward * speed.getValue() * rx)
                            + (strafe * speed.getValue() * rz)), (forward * speed.getValue() * rz)
                            - (strafe * speed.getValue() * rx));
                }
                MoneyStar.movementManager.setMotionY(0.0);
                pitch = 0;
                if (Util.mc.options.jumpKey.isPressed()) {
                    pitch = -51;
                    MoneyStar.movementManager.setMotionY(vspeed.getValue());
                } else if (Util.mc.options.sneakKey.isPressed()) {
                    MoneyStar.movementManager.setMotionY(-vspeed.getValue());
                }
            }
            case BOOST -> {
                event.cancel();
                Util.mc.player.limbAnimator.setSpeed(0.0f);
                glideElytraVec(Util.mc.player.getPitch());
                boolean boost = Util.mc.options.jumpKey.isPressed();
                float yaw = Util.mc.player.getYaw() * 0.017453292f;
                if (boost) {
                    double sin = -MathHelper.sin(yaw);
                    double cos = MathHelper.cos(yaw);
                    double motionX = sin * speed.getValue() / 20.0f;
                    double motionZ = cos * speed.getValue() / 20.0f;
                    MoneyStar.movementManager.setMotionXZ(Util.mc.player.getVelocity().x + motionX,
                            Util.mc.player.getVelocity().z + motionZ);
                }
            }
        }
    }

//    @EventListener
//    public void onRemoveFirework(RemoveFireworkEvent event)
//    {
//        if (mc.player == null)
//        {
//            return;
//        }
//        if (mc.player.isFallFlying() && event.getRocketEntity() != fireworkRocketEntity
//                && fireworkConfig.getValue())
//        {
//            fireworkRocketEntity = event.getRocketEntity();
//            boostFirework();
//        }
//    }

    @Subscribe
    public void onPacketOutbound(PacketEvent event) {
        if (Util.mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet
                && packet.changesLook() && Util.mc.player.isGliding()) {
            if (mode.getValue() == FlyMode.CONTROL) {
                if (Util.mc.options.leftKey.isPressed()) {
                    ((AccessorPlayerMoveC2SPacket) packet).hookSetYaw(packet.getYaw(0.0f) - 90.0f);
                }
                if (Util.mc.options.rightKey.isPressed()) {
                    ((AccessorPlayerMoveC2SPacket) packet).hookSetYaw(packet.getYaw(0.0f) + 90.0f);
                }
            }
            ((AccessorPlayerMoveC2SPacket) packet).hookSetPitch(pitch);
        }
    }

    private void boostFirework() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Util.mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof FireworkRocketItem) {
                slot = i;
                break;
            }
        }
        if (slot != -1) {
            int prev = Util.mc.player.getInventory().selectedSlot;
            MoneyStar.inventoryManager.setClientSlot(slot);
            Util.mc.interactionManager.interactItem(Util.mc.player, Hand.MAIN_HAND);
            MoneyStar.inventoryManager.setClientSlot(prev);
        }
    }

    private void glideElytraVec(float pitch) {
        double d = 0.08;
        boolean bl = Util.mc.player.getVelocity().y <= 0.0;
        if (bl && Util.mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            d = 0.01;
        }
        Vec3d vec3d4 = Util.mc.player.getVelocity();
        Vec3d vec3d5 = getRotationVector(pitch, Util.mc.player.getYaw());
        float f = pitch * 0.017453292f;
        double i = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
        double j = vec3d4.horizontalLength();
        double k = vec3d5.length();
        double l = Math.cos(f);
        l = l * l * Math.min(1.0, k / 0.4);
        vec3d4 = Util.mc.player.getVelocity().add(0.0, d * (-1.0 + l * 0.75), 0.0);
        double m;
        // if (vec3d4.y < 0.0 && i > 0.0)
        // {
        //    m = vec3d4.y * -0.1 * l;
        //    vec3d4 = vec3d4.add(vec3d5.x * m / i, m, vec3d5.z * m / i);
        // }
        if (f < 0.0f && i > 0.0) {
            m = j * (double) (-MathHelper.sin(f)) * 0.04;
            vec3d4 = vec3d4.add(-vec3d5.x * m / i, m * 3.2, -vec3d5.z * m / i);
        }
        // if (i > 0.0)
        // {
        //     vec3d4 = vec3d4.add((vec3d5.x / i * j - vec3d4.x) * 0.1, 0.0, (vec3d5.z / i * j - vec3d4.z) * 0.1);
        // }
        Util.mc.player.setVelocity(vec3d4.multiply(0.9900000095367432, 0.9800000190734863, 0.9900000095367432));
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292f;
        float g = -yaw * 0.017453292f;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double) (i * j), (double) (-k), (double) (h * j));
    }

    public enum FlyMode {
        CONTROL,
        BOOST,

    }
}
