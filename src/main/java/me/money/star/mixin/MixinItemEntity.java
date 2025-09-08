package me.money.star.mixin;

import me.money.star.event.impl.ItemTickEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class MixinItemEntity
{
    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    private void hookTick(CallbackInfo ci)
    {
        ItemTickEvent itemTickEvent = new ItemTickEvent();
        Util.EVENT_BUS.post(itemTickEvent);
        if (itemTickEvent.isCancelled())
        {
            ci.cancel();
        }
    }
}
