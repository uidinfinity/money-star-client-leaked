package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class SkyboxEvent extends Event
{
    private Color color;

    public Color getColor()
    {
        return color;
    }

    public Vec3d getColorVec()
    {
        return new Vec3d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
    }

    public int getRGB()
    {
        return color.getRGB();
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    @Cancelable
    public static class Sky extends SkyboxEvent
    {

    }

    @Cancelable
    public static class Cloud extends SkyboxEvent
    {

    }

    @Cancelable
    public static class Fog extends SkyboxEvent
    {
        private final float tickDelta;

        public Fog(float tickDelta)
        {
            this.tickDelta = tickDelta;
        }

        public float getTickDelta()
        {
            return tickDelta;
        }
    }
}
