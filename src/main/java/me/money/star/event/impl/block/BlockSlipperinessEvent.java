package me.money.star.event.impl.block;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.block.Block;


/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class BlockSlipperinessEvent extends Event {
    //
    private final Block block;
    private float slipperiness;

    /**
     * @param block
     * @param slipperiness
     */
    public BlockSlipperinessEvent(Block block, float slipperiness) {
        this.block = block;
        this.slipperiness = slipperiness;
    }

    /**
     * @return
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @return
     */
    public float getSlipperiness() {
        return slipperiness;
    }

    /**
     * @param slipperiness
     */
    public void setSlipperiness(float slipperiness) {
        this.slipperiness = slipperiness;
    }
}
