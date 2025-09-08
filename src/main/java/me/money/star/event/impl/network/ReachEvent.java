package me.money.star.event.impl.network;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

@Cancelable
public class ReachEvent extends Event {
    private float reach;

    public float getReach() {
        return reach;
    }

    public void setReach(float reach) {
        this.reach = reach;
    }
}
