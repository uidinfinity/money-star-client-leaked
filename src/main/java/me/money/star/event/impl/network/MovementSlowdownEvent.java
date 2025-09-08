package me.money.star.event.impl.network;

import me.money.star.event.Event;
import net.minecraft.client.input.Input;

/**
 * @author linus
 * @since 1.0
 */
public class MovementSlowdownEvent extends Event {
    //
    public final Input input;

    /**
     * @param input
     */
    public MovementSlowdownEvent(Input input) {
        this.input = input;
    }

    /**
     * @return
     */
    public Input getInput() {
        return input;
    }
}
