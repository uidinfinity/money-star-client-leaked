package me.money.star.event.impl.network;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class TickMovementEvent extends Event {
    //
    private int iterations;

    /**
     * @return
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * @param iterations
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
