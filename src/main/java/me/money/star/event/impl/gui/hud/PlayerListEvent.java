package me.money.star.event.impl.gui.hud;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

@Cancelable
public class PlayerListEvent extends Event {

    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
