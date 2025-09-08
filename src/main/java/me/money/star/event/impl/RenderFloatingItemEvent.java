package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Cancelable
public class RenderFloatingItemEvent extends Event
{
    private final ItemStack floatingItem;

    public RenderFloatingItemEvent(ItemStack floatingItem)
    {
        this.floatingItem = floatingItem;
    }

    public Item getFloatingItem()
    {
        return floatingItem.getItem();
    }

    public ItemStack getFloatingItemStack()
    {
        return floatingItem;
    }
}
