package me.money.star.event.impl.world;

import me.money.star.event.Event;
import net.minecraft.entity.Entity;

public class AddEntityEvent extends Event {
    private final Entity entity;

    public AddEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
