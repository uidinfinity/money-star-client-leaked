package me.money.star.event.impl.render.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.ItemEntity;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class RenderItemEvent extends Event {
    private final ItemEntity itemEntity;

    public RenderItemEvent(ItemEntity itemEntity) {
        this.itemEntity = itemEntity;
    }

    public ItemEntity getItem() {
        return itemEntity;
    }
}
