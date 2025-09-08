package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.modules.render.SkyColors;
import me.money.star.event.impl.BlindnessEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import java.awt.*;


@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer
{
    @ModifyArgs(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Fog;<init>(FFLnet/minecraft/client/render/FogShape;FFFF)V"))
    private static void applyFog(Args args, Camera camera, BackgroundRenderer.FogType fogType, Vector4f originalColor, float viewDistance, boolean thickenFog, float tickDelta) {
        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN ) {
            args.set(0, viewDistance * 4);
            args.set(1, viewDistance * 4.25f);
        } else {
            Color color = new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

            if (MoneyStar.moduleManager.getModuleByClass(SkyColors.class).isEnabled()) {
                args.set(3, color.getRed() / 255.0f);
                args.set(4, color.getGreen() / 255.0f);
                args.set(5, color.getBlue() / 255.0f);
                args.set(6, color.getAlpha() / 255.0f);
            }
        }
    }


}
