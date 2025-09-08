package me.money.star.event.impl.render;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

@Cancelable
public class FovEvent extends Event {
    private double fov;

    public double getFov() {
        return fov;
    }

    public void setFov(double fov) {
        this.fov = fov;
    }
}
