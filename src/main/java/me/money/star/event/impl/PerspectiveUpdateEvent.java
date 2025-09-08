package me.money.star.event.impl;

import me.money.star.event.Event;
import net.minecraft.client.option.Perspective;


public class PerspectiveUpdateEvent extends Event
{
    private final Perspective perspective;

    public PerspectiveUpdateEvent(Perspective perspective)
    {
        this.perspective = perspective;
    }

    public Perspective getPerspective()
    {
        return perspective;
    }
}
