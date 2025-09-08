package me.money.star.event.impl.entity.decoration;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.Entity;


/**
 *
 */
@Cancelable
public class TeamColorEvent extends Event {
    private final Entity entity;
    private int color;

    public TeamColorEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
