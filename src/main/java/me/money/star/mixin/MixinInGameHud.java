package me.money.star.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.money.star.MoneyStar;
import me.money.star.client.modules.render.CrossHair;
import me.money.star.event.impl.Render2DEvent;
import me.money.star.event.impl.gui.hud.RenderOverlayEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Identifier;


import static me.money.star.util.traits.Util.mc;

@Mixin( InGameHud.class )
public class MixinInGameHud {

    //
    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud()) return;
        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.disableCull();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);


        Render2DEvent event = new Render2DEvent(context, tickCounter.getTickDelta(true));
        Util.EVENT_BUS.post(event);

        RenderSystem.enableDepthTest();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

    }
    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        if(MoneyStar.moduleManager.getModuleByClass(CrossHair.class).isEnabled()) info.cancel();
    }
    @Inject(method = "render", at = @At(value = "TAIL"))
    private void hookRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderOverlayEvent.Post renderOverlayEvent =
                new RenderOverlayEvent.Post(context, tickCounter.getTickDelta(true));
        Util.EVENT_BUS.post(renderOverlayEvent);
    }

//    @Redirect(method = "renderHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I"))
//    private int hookRenderHotbar$selectedSlot(PlayerInventory instance) {
//        return Managers.INVENTORY.getServerSlot();
//    }

    /**
     * @param context
     * @param ci
     */
    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderOverlayEvent.StatusEffect renderOverlayEvent =
                new RenderOverlayEvent.StatusEffect(context);
        Util.EVENT_BUS.post(renderOverlayEvent);
        if (renderOverlayEvent.isCancelled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param nauseaStrength
     * @param ci
     */
    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void hookRenderPortalOverlay(DrawContext context,
                                         float nauseaStrength,
                                         CallbackInfo ci)
    {
        RenderOverlayEvent.Portal renderOverlayEvent = new RenderOverlayEvent.Portal(context);
        Util.EVENT_BUS.post(renderOverlayEvent);
        if (renderOverlayEvent.isCancelled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param scale
     * @param ci
     */
    @Inject(method = "renderSpyglassOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRenderSpyglassOverlay(DrawContext context, float scale,
                                           CallbackInfo ci)
    {
        RenderOverlayEvent.Spyglass renderOverlayEvent =
                new RenderOverlayEvent.Spyglass(context);
        Util.EVENT_BUS.post(renderOverlayEvent);
        if (renderOverlayEvent.isCancelled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param texture
     * @param opacity
     * @param ci
     */
    @Inject(method = "renderOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderOverlay(DrawContext context, Identifier texture,
                                   float opacity, CallbackInfo ci)
    {

        if (texture.getPath().equals(POWDER_SNOW_OUTLINE.getPath()))
        {
            RenderOverlayEvent.Frostbite renderOverlayEvent =
                    new RenderOverlayEvent.Frostbite(context);
            Util.EVENT_BUS.post(renderOverlayEvent);
            if (renderOverlayEvent.isCancelled())
            {
                ci.cancel();
            }
        }
    }

    /**
     * @param instance
     * @param text
     * @param x
     * @param y
     * @param color
     * @return
     */
    @Redirect(method = "renderHeldItemTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"))
    private int hookRenderHeldItemTooltip(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int width, int color)
    {
        RenderOverlayEvent.ItemName renderOverlayEvent =
                new RenderOverlayEvent.ItemName(instance);
        Util.EVENT_BUS.post(renderOverlayEvent);
        if (renderOverlayEvent.isCancelled())
        {
            if (renderOverlayEvent.isUpdateXY())
            {
                return instance.drawText(mc.textRenderer, text,
                        renderOverlayEvent.getX(), renderOverlayEvent.getY(), color, true);
            }
            return 0;
        }
        return instance.drawText(mc.textRenderer, text, x, y, color, true);
    }

    @Inject(method = "renderMainHud", at = @At(value = "TAIL"))
    private void hookRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderOverlayEvent.Hotbar hotbar = new RenderOverlayEvent.Hotbar(context);
        Util.EVENT_BUS.post(hotbar);
    }

}
