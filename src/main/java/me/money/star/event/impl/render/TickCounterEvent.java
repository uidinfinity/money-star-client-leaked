package me.money.star.event.impl.render;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class TickCounterEvent extends Event {
    //
    private float ticks;

    /**
     * @return
     */
    public float getTicks() {
        return ticks;
    }

    /**
     * @param ticks
     */
    public void setTicks(float ticks) {
        this.ticks = ticks;
    }
}
