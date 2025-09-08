package me.money.star.event.impl.network;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;



@Cancelable
public class MovementPacketsEvent extends Event {
    //
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private boolean horizontalCollision;

    public MovementPacketsEvent(double x, double y, double z, float yaw, float pitch, boolean onGround,boolean horizontalCollision) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.horizontalCollision = horizontalCollision;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
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

    public boolean getHorizontalCollision() {
        return horizontalCollision;
    }
    public boolean getOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
