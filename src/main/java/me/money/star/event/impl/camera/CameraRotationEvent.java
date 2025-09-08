package me.money.star.event.impl.camera;

import me.money.star.event.Event;
import net.minecraft.util.math.Vec2f;

public class CameraRotationEvent extends Event {

    private float yaw;
    private float pitch;
    private final float tickDelta;

    public CameraRotationEvent(float yaw, float pitch, float tickDelta) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.tickDelta = tickDelta;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRotation(Vec2f rotation) {
        yaw = rotation.x;
        pitch = rotation.y;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
