package me.money.star.event.impl.text;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import me.money.star.mixin.MixinTextVisitFactory;

/**
 * @see MixinTextVisitFactory
 */
@Cancelable
public class TextVisitEvent extends Event {
    //
    private String text;

    /**
     * @param text
     */
    public TextVisitEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
