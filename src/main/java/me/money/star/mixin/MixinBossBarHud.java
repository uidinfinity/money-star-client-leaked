package me.money.star.mixin;

import me.money.star.event.impl.gui.hud.RenderOverlayEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BossBarHud.class)
public class MixinBossBarHud {
    /**
     * @param context
     * @param ci
     */
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void hookRender(DrawContext context, CallbackInfo ci) {
        RenderOverlayEvent.BossBar renderOverlayEvent =
                new RenderOverlayEvent.BossBar(context);
        Util.EVENT_BUS.post(renderOverlayEvent);
        if (renderOverlayEvent.isCancelled()) {
            ci.cancel();
        }
    }
}
