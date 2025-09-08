package me.money.star.event.impl.render.block;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * @author linus
 * @see net.minecraft.client.render.block.BlockModelRenderer
 * @since 1.0
 */
@Cancelable
public class RenderBlockEvent extends Event {
    private final BlockState state;
    private final BlockPos pos;

    public RenderBlockEvent(BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    public BlockState getState() {
        return state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Block getBlock() {
        return state.getBlock();
    }
}
