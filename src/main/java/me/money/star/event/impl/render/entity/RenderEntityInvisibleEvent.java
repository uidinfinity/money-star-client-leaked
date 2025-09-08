package me.money.star.event.impl.render.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.LivingEntity;

@Cancelable
public class RenderEntityInvisibleEvent extends Event {
    private final LivingEntity entity;

    public RenderEntityInvisibleEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
