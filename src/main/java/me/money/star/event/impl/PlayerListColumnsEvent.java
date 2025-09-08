package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

@Cancelable
public class PlayerListColumnsEvent extends Event
{
    private int tabHeight;

    public void setTabHeight(int tabHeight)
    {
        this.tabHeight = tabHeight;
    }

    public int getTabHeight()
    {
        return tabHeight;
    }
}
