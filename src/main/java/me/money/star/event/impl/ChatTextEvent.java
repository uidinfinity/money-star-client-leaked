package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.text.OrderedText;


@Cancelable
public class ChatTextEvent extends Event
{

    private OrderedText text;

    public ChatTextEvent(OrderedText text)
    {
        this.text = text;
    }

    public void setText(OrderedText text)
    {
        this.text = text;
    }

    public OrderedText getText()
    {
        return text;
    }
}
