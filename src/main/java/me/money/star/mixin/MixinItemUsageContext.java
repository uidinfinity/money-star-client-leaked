package me.money.star.mixin;

import me.money.star.event.impl.ItemDesyncEvent;
import me.money.star.util.traits.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemUsageContext.class)
public final class MixinItemUsageContext implements Util
{
    @Inject(method = "getStack", at = @At("RETURN"), cancellable = true)
    public void hookGetStack(final CallbackInfoReturnable<ItemStack> info)
    {
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        Util.EVENT_BUS.post(itemDesyncEvent);
        if (mc.player != null && info.getReturnValue().equals(mc.player.getMainHandStack()) && itemDesyncEvent.isCancelled())
        {
            info.setReturnValue(itemDesyncEvent.getServerItem());
        }
    }
}
