package me.money.star.event.impl.render.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.client.network.AbstractClientPlayerEntity;


@Cancelable
public class RenderPlayerEvent extends Event {

    //
    private final AbstractClientPlayerEntity entity;
    //
    private float yaw;
    private float pitch;

    /**
     * @param entity
     */
    public RenderPlayerEvent(AbstractClientPlayerEntity entity)
    {
        this.entity = entity;
    }

    /**
     * @return
     */
    public AbstractClientPlayerEntity getEntity()
    {
        return entity;
    }


    public float getYaw()
    {
        return yaw;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }
}