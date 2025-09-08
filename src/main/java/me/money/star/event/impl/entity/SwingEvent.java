package me.money.star.event.impl.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.util.Hand;

@Cancelable
public class SwingEvent extends Event {
    private final Hand hand;

    public SwingEvent(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }
}
