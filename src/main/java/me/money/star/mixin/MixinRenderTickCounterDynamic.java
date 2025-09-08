package me.money.star.mixin;


import me.money.star.MoneyStar;
import me.money.star.client.modules.exploit.Timer;
import me.money.star.event.impl.render.TickCounterEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class MixinRenderTickCounterDynamic {
    @Shadow
    private float lastFrameDuration;

    @Shadow
    private float tickDelta;

    @Shadow
    private long prevTimeMillis;

    @Final
    @Shadow
    private float tickTime;

    /**
     * @param timeMillis
     * @param cir
     */
    @Inject(method = "beginRenderTick(J)I", at = @At(value = "HEAD"), cancellable = true)
    private void hookBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir)
    {
        TickCounterEvent tickCounterEvent = new TickCounterEvent();
        Util.EVENT_BUS.post(tickCounterEvent);
        if (tickCounterEvent.isCancelled())
        {
            lastFrameDuration = ((timeMillis - prevTimeMillis) / tickTime) * tickCounterEvent.getTicks();
            prevTimeMillis = timeMillis;
            tickDelta += lastFrameDuration;
            int i = (int) tickDelta;
            tickDelta -= i;
            cir.setReturnValue(i);
        }
    }


}
