package me.money.star.event.impl.network;

import me.money.star.event.StageEvent;


public class PlayerUpdateEvent extends StageEvent {
    private float yaw;
    private float pitch;

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
}
