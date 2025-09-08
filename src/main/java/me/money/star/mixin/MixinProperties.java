package me.money.star.mixin;


import me.money.star.MoneyStar;
import me.money.star.client.modules.render.Environment;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public class MixinProperties {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void getTimeOfDay(CallbackInfoReturnable<Long> info) {
        if (MoneyStar.moduleManager.getModuleByClass(Environment.class).isEnabled() && MoneyStar.moduleManager.getModuleByClass(Environment.class).timeChange.getValue()) {
            info.setReturnValue(MoneyStar.moduleManager.getModuleByClass(Environment.class).time.getValue() * 100L);
        }
    }
}
