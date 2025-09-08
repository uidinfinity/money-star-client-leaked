package me.money.star.event.impl.item;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;

@Cancelable
public class DurabilityEvent extends Event {
    //
    private int damage;

    public DurabilityEvent(int damage) {
        this.damage = damage;
    }

    public int getItemDamage() {
        return Math.max(0, damage);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
