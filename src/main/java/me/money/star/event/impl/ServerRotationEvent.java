package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;


@Cancelable
public class ServerRotationEvent extends Event
{
    private float yaw = Float.NaN;
    private float pitch = Float.NaN;

    public float getYaw()
    {
        return yaw;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }
}
