package me.money.star.mixin;

import me.money.star.event.impl.block.SteppedOnSlimeBlockEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @see SlimeBlock
 * @since 1.0
 */
@Mixin(SlimeBlock.class)
public class MixinSlimeBlock implements Util {
    /**
     * @param world
     * @param pos
     * @param state
     * @param entity
     * @param ci
     */
    @Inject(method = "onSteppedOn", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookOnSteppedOn(World world, BlockPos pos, BlockState state,
                                 Entity entity, CallbackInfo ci) {
        SteppedOnSlimeBlockEvent steppedOnSlimeBlockEvent =
                new SteppedOnSlimeBlockEvent();
        Util.EVENT_BUS.post(steppedOnSlimeBlockEvent);
        if (steppedOnSlimeBlockEvent.isCancelled() && entity == mc.player) {
            ci.cancel();
        }
    }
}
