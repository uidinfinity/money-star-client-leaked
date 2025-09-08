package me.money.star.event.impl.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class VelocityMultiplierEvent extends Event {
    //
    private final BlockState state;

    /**
     * @param state
     */
    public VelocityMultiplierEvent(BlockState state) {
        this.state = state;
    }

    /**
     * @return
     */
    public Block getBlock() {
        return state.getBlock();
    }
}
