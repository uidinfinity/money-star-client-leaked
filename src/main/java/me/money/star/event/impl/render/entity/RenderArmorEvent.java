package me.money.star.event.impl.render.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.LivingEntity;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class RenderArmorEvent extends Event {
    private final LivingEntity entity;

    public RenderArmorEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
