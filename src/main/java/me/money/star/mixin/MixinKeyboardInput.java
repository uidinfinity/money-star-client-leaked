package me.money.star.mixin;

import me.money.star.event.Stage;
import me.money.star.event.impl.keyboard.KeyboardTickEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(KeyboardInput.class)
public class MixinKeyboardInput
{
 /*
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void hookTick$Pre(boolean slowDown, float slowDownFactor, CallbackInfo info)
    {
        KeyboardTickEvent event = new KeyboardTickEvent((Input) (Object) this);
        event.setStage(Stage.PRE);
        Util.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            info.cancel();
        }
    }

    /**
     * @param slowDown
     * @param f
     * @param ci
     */
    /*
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/" +
            "client/input/KeyboardInput;sneaking:Z", shift = At.Shift.BEFORE), cancellable = true)
    private void hookTick$Post(boolean slowDown, float f, CallbackInfo ci)
    {
        KeyboardTickEvent keyboardTickEvent = new KeyboardTickEvent((Input) (Object) this);
        keyboardTickEvent.setStage(Stage.POST);
        Util.EVENT_BUS.post(keyboardTickEvent);
        if (keyboardTickEvent.isCancelled())
        {
            ci.cancel();
        }
    }

     */
}
