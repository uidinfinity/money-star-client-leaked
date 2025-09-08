package me.money.star.event.impl.particle;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

import java.awt.*;

@Cancelable
public class TotemParticleEvent extends Event {
    private Color color;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
