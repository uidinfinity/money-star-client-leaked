package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

/**
 *
 */
@Cancelable
public class FramerateLimitEvent extends Event {

    private int framerateLimit;

    public int getFramerateLimit() {
        return framerateLimit;
    }

    public void setFramerateLimit(int framerateLimit) {
        this.framerateLimit = framerateLimit;
    }
}
