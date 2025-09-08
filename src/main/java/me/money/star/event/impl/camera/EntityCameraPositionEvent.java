package me.money.star.event.impl.camera;

import me.money.star.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityCameraPositionEvent extends Event {

    private Vec3d position;
    private final float tickDelta;
    private final Entity entity;

    public EntityCameraPositionEvent(Vec3d position, Entity entity, float tickDelta) {
        this.position = position;
        this.tickDelta = tickDelta;
        this.entity = entity;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public Entity getEntity() {
        return entity;
    }
}