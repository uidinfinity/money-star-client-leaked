package me.money.star.event.impl.render.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.Entity;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class RenderLabelEvent extends Event {
    private final Entity entity;

    public RenderLabelEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
