package me.money.star.event.impl.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.block.BlockState;

@Cancelable
public class SlowMovementEvent extends Event {
    private final BlockState state;

    public SlowMovementEvent(BlockState state) {
        this.state = state;
    }

    public BlockState getState() {
        return state;
    }
}
