package me.money.star.event.impl.render;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;


@Cancelable
public class LightmapGammaEvent extends Event
{
    //
    private int gamma;

    /**
     * @param gamma
     */
    public LightmapGammaEvent(int gamma)
    {
        this.gamma = gamma;
    }

    public int getGamma()
    {
        return gamma;
    }

    public void setGamma(int gamma)
    {
        this.gamma = gamma;
    }
}
