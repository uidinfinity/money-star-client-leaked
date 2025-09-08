package me.money.star.mixin;

import me.money.star.event.impl.entity.passive.EntitySteerEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public class MixinAbstractHorseEntity {
    /**
     * @param cir
     */
    @Inject(method = "isSaddled", at = @At(value = "HEAD"), cancellable = true)
    private void hookIsSaddled(CallbackInfoReturnable<Boolean> cir) {
        EntitySteerEvent entitySteerEvent = new EntitySteerEvent();
        Util.EVENT_BUS.post(entitySteerEvent);
        if (entitySteerEvent.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
