package me.money.star.mixin;

import me.money.star.event.impl.RenderSkylightEvent;
import me.money.star.util.traits.Util;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkylightProvider
{
    /**
     * @param blockPos
     * @param l
     * @param lightLevel
     * @param ci
     */
    @Inject(method = "method_51531", at = @At(value = "HEAD"), cancellable = true)
    private void hookRecalculateLevel(long blockPos, long l, int lightLevel, CallbackInfo ci)
    {
        RenderSkylightEvent renderSkylightEvent = new RenderSkylightEvent();
        Util.EVENT_BUS.post(renderSkylightEvent);
        if (renderSkylightEvent.isCancelled())
        {
            ci.cancel();
        }
    }
}
