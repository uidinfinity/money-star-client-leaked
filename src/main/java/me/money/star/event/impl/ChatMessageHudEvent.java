package me.money.star.event.impl;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.text.Text;

@Cancelable
public class ChatMessageHudEvent extends Event
{
    private Text text;

    public ChatMessageHudEvent(Text text)
    {
        this.text = text;
    }

    public void setText(Text text)
    {
        this.text = text;
    }

    public Text getText()
    {
        return text;
    }
}
