package me.money.star.event.impl.entity.player;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.Entity;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class PushEntityEvent extends Event {
    private final Entity pushed, pusher;

    public PushEntityEvent(Entity pushed, Entity pusher) {
        this.pushed = pushed;
        this.pusher = pusher;
    }

    public Entity getPushed() {
        return pushed;
    }

    public Entity getPusher() {
        return pusher;
    }
}
