package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.client.render.Camera;

@Cancelable
public class PerspectiveEvent extends Event {

    public Camera camera;

    public PerspectiveEvent(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

}
