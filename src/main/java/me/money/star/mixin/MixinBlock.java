package me.money.star.mixin;

import me.money.star.event.impl.block.BlockSlipperinessEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(Block.class)
public class MixinBlock {
    /**
     * @param cir
     */
    @Inject(method = "getSlipperiness", at = @At(value = "RETURN"),
            cancellable = true)
    private void hookGetSlipperiness(CallbackInfoReturnable<Float> cir) {
        BlockSlipperinessEvent blockSlipperinessEvent =
                new BlockSlipperinessEvent((Block) (Object) this, cir.getReturnValueF());
        Util.EVENT_BUS.post(blockSlipperinessEvent);
        if (blockSlipperinessEvent.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(blockSlipperinessEvent.getSlipperiness());
        }
    }
}
