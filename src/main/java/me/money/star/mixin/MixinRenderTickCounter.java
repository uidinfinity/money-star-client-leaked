package me.money.star.mixin;


import me.money.star.MoneyStar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin( RenderTickCounter.Dynamic.class )
public class MixinRenderTickCounter {
    @Shadow
    private float lastFrameDuration;

    @Inject(method = "beginRenderTick(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J"))
    public void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.lastFrameDuration *= MoneyStar.TIMER;
    }
}