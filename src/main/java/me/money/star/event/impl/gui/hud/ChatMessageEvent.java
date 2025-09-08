package me.money.star.event.impl.gui.hud;

import me.money.star.event.Event;
import net.minecraft.text.Text;


public class ChatMessageEvent extends Event {
    private final Text text;

    public ChatMessageEvent(Text text) {
        this.text = text;
    }

    public Text getText() {
        return text;
    }
}
