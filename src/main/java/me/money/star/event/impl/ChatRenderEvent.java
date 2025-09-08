package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.client.gui.DrawContext;

@Cancelable
public class ChatRenderEvent extends Event
{
    //
    private final DrawContext context;
    private final float x, y;

    public ChatRenderEvent(DrawContext context, float x, float y)
    {
        this.context = context;
        this.x = x;
        this.y = y;
    }

    public DrawContext getContext()
    {
        return context;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }
}
