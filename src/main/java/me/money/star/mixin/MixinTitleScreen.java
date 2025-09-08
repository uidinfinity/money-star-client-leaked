package me.money.star.mixin;

import me.money.star.client.modules.client.Colors;
import me.money.star.util.render.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;

/**
 * @author xgraza
 * @since 03/28/24
 */
@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    @Shadow
    @Nullable
    private SplashTextRenderer splashText;

    @Shadow
    @Final
    public static Text COPYRIGHT;
/*
    @Shadow
    protected abstract void initWidgetsDemo(int y, int spacingY);

    @Shadow
    protected abstract void initWidgetsNormal(int y, int spacingY);

 */

    @Shadow
    @Nullable
    private RealmsNotificationsScreen realmsNotificationGui;

    @Shadow
    protected abstract boolean isRealmsNotificationsGuiDisplayed();

    @Shadow
    private long backgroundFadeStart;

    @Shadow
    @Final
    private boolean doBackgroundFade;

    public MixinTitleScreen(Text title) {
        super(title);
    }
    public int color = ColorUtil.toARGB(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);


    @Inject(method = "render", at = @At("TAIL"))
    public void hookRender(final DrawContext context, final int mouseX, final int mouseY, final float delta, final CallbackInfo info) {
        float f = this.doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0f : 1.0f;
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0f, 0.0f, 1.0f) : 1.0f;
        int i = MathHelper.ceil(g * 255.0f) << 24;
        if ((i & 0xFC000000) == 0) {
            return;
        }
        context.drawTextWithShadow(client.textRenderer,
                "Money"+Formatting.WHITE+"-Star"+Formatting.GRAY+" (build: "+new Date().toString()+")",
                2, height - (client.textRenderer.fontHeight * 2) - 2, color | i);


/*
    @Inject(method = "initWidgetsNormal", at = @At(
            target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
            value = "INVOKE", shift = At.Shift.AFTER, ordinal = 2))
    public void hookInit(int y, int spacingY, CallbackInfo ci) {
        // parameters from when the method initWidgetsNormal is called
        final ButtonWidget widget = ButtonWidget.builder(Text.of("Account Manager"), (action) -> client.setScreen(new AccountSelectorScreen((Screen) (Object) this)))
            .dimensions(this.width / 2 - 100, y + spacingY * 3, 200, 20)
            .tooltip(Tooltip.of(Text.of("Allows you to switch your in-game account")))
            .build();
        widget.active = true;
        addDrawableChild(widget);
    }

 */
}
}
