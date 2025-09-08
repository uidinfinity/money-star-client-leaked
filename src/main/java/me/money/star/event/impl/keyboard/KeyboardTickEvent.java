package me.money.star.event.impl.keyboard;

import me.money.star.event.Cancelable;
import me.money.star.event.StageEvent;
import net.minecraft.client.input.Input;


@Cancelable
public class KeyboardTickEvent extends StageEvent {

    private final Input input;

    public KeyboardTickEvent(Input input)
    {
        this.input = input;
    }

    public Input getInput()
    {
        return input;
    }
}
