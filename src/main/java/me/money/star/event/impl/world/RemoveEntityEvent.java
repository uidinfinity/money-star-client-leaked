package me.money.star.event.impl.world;

import me.money.star.event.Event;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;


public class RemoveEntityEvent extends Event implements Util {
    private final Entity entity;
    private final Entity.RemovalReason removalReason;

    public RemoveEntityEvent(Entity entity, Entity.RemovalReason removalReason) {
        this.entity = entity;
        this.removalReason = removalReason;
    }

    /**
     * @return
     */
    public Entity getEntity() {
        return entity;
    }

    public Entity.RemovalReason getRemovalReason() {
        return removalReason;
    }
}
