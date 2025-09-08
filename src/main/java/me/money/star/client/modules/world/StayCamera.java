package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.manager.player.rotation.Rotation;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.*;
import me.money.star.event.impl.camera.CameraPositionEvent;
import me.money.star.event.impl.camera.CameraRotationEvent;
import me.money.star.event.impl.camera.EntityCameraPositionEvent;
import me.money.star.event.impl.entity.EntityDeathEvent;
import me.money.star.event.impl.entity.EntityRotationVectorEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.util.player.RayCastUtil;
import me.money.star.util.player.RotationUtil;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;


public final class StayCamera extends Module {
    private static StayCamera INSTANCE;
    public Setting<Boolean> rotate = bool("Rotate", false);


    public Vec3d position, lastPosition;

    public float yaw, pitch;

    public boolean control = false;
    public StayCamera() {
        super("StayCamera", "Allows you to freely move the camera in third person", Category.WORLD, true, false, false);
        INSTANCE = this;

    }

    public static StayCamera getInstance()
    {
        return INSTANCE;
    }
    @Override
    public void onEnable()
    {
        if (mc.player == null) return;
        control = false;

        position = mc.gameRenderer.getCamera().getPos();
        lastPosition = position;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        mc.player.input = new FreecamKeyboardInput(mc.options);
    }

    @Override
    public void onDisable()
    {
        if (mc.player == null) return;
        mc.player.input = new KeyboardInput(mc.options);
    }

    @Subscribe
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            disable();
        }
    }



    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        disable();
    }

    @Subscribe
    public void onCameraPosition(CameraPositionEvent event)
    {
        event.setPosition(control ? position : lastPosition.lerp(position, event.getTickDelta()));
    }

    @Subscribe
    public void onCameraRotation(CameraRotationEvent event)
    {
        event.setRotation(new Vec2f(yaw, pitch));
    }

    @Subscribe
    public void onMouseUpdate(MouseUpdateEvent event)
    {
        if (!control)
        {
            event.cancel();
            changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
        }
    }

    @Subscribe
    public void onEntityCameraPosition(EntityCameraPositionEvent event)
    {
        if (event.getEntity() != mc.player) return;
        if (!control )
        {
            event.setPosition(position);
        }
    }

    @Subscribe
    public void onEntityRotation(EntityRotationVectorEvent event)
    {
        if (event.getEntity() != mc.player) return;
        if (!control)
        {
            event.setPosition(RotationUtil.getRotationVector(pitch, yaw));
        }
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (event.getStage() != Stage.PRE) return;
        if (!control)
        {
            float[] currentAngles = {yaw, pitch};
            Vec3d eyePos = position;
            HitResult result = RayCastUtil.rayCast(mc.player.getBlockInteractionRange(), eyePos, currentAngles);
            if (result.getType() == HitResult.Type.BLOCK)
            {
                float[] newAngles = RotationUtil.getRotationsTo(mc.player.getEyePos(), result.getPos());
                MoneyStar.rotationManager.setRotation(new Rotation(1, newAngles[0], newAngles[1]));
            }
        }
    }

    // Render the player in third person
    @Subscribe
    public void onPerspective(PerspectiveEvent event)
    {
        event.cancel();
    }

    @Subscribe
    public void onRenderArm(RenderFirstPersonEvent.Head event)
    {
        event.cancel();
    }



    public class FreecamKeyboardInput extends KeyboardInput
    {

        private final GameOptions options;

        public FreecamKeyboardInput(GameOptions options)
        {
            super(options);
            this.options = options;
        }


    }

  
    private float getMovementMultiplier(boolean positive, boolean negative)
    {
        if (positive == negative)
        {
            return 0.0F;
        }
        else
        {
            return positive ? 1.0F : -1.0F;
        }
    }

  
    private Vec2f handleVanillaMotion(final float speed, float forward, float strafe)
    {
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        }
        else if (forward != 0.0f && strafe != 0.0f)
        {
            forward *= (float) Math.sin(0.7853981633974483);
            strafe *= (float) Math.cos(0.7853981633974483);
        }
        return new Vec2f((float) (forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw))),
                (float) (forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw))));
    }

    /**
     * @param cursorDeltaX
     * @param cursorDeltaY
     * @see net.minecraft.entity.Entity#changeLookDirection(double, double)
     */
    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY)
    {
        float f = (float) cursorDeltaY * 0.15F;
        float g = (float) cursorDeltaX * 0.15F;
        this.pitch += f;
        this.yaw += g;
        this.pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
    }

    public Vec3d getCameraPosition()
    {
        return position;
    }

    public Vec3d getLastCameraPosition()
    {
        return lastPosition;
    }

    public float[] getCameraRotations()
    {
        return new float[]{yaw, pitch};
    }

    public enum Interact
    {
        PLAYER,
        CAMERA
    }
}

