package me.money.star.event.impl.entity;


import me.money.star.event.Event;

public final class JumpRotationEvent extends Event {
    private float yaw;


    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
