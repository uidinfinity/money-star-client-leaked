package me.money.star.event.impl.network;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

@Cancelable
public class MountJumpStrengthEvent extends Event {
    //
    private float jumpStrength;

    public float getJumpStrength() {
        return jumpStrength;
    }

    public void setJumpStrength(float jumpStrength) {
        this.jumpStrength = jumpStrength;
    }
}
