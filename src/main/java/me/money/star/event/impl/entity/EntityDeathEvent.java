package me.money.star.event.impl.entity;

import me.money.star.event.Event;
import net.minecraft.entity.LivingEntity;

public class EntityDeathEvent extends Event {

    private final LivingEntity entity;

    public EntityDeathEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
