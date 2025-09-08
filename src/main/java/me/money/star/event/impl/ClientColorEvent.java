package me.money.star.event.impl;

import me.money.star.event.Event;


public class ClientColorEvent extends Event
{

    private int rgb;

    public void setRgb(int rgb)
    {
        this.rgb = rgb;
    }

    public int getClientRgb()
    {
        return rgb;
    }

    public static class Friend extends ClientColorEvent
    {

    }
}
