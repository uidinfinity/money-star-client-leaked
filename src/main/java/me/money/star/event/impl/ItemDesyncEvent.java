package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.item.ItemStack;


@Cancelable
public class ItemDesyncEvent extends Event
{

    private ItemStack stack;


    public void setStack(ItemStack stack)
    {
        this.stack = stack;
    }

    public ItemStack getServerItem()
    {
        return stack;
    }
}
