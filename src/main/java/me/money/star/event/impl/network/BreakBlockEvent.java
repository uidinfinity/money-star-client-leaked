package me.money.star.event.impl.network;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.util.math.BlockPos;

@Cancelable
public class BreakBlockEvent extends Event {
    private final BlockPos pos;

    public BreakBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
