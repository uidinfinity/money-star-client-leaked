package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.Entity;

@Cancelable
public class EntityOutlineEvent extends Event {
    private final Entity entity;

    public EntityOutlineEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
