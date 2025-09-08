package me.money.star.mixin;

import me.money.star.event.impl.text.TextVisitEvent;
import me.money.star.util.traits.Util;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(TextVisitFactory.class)
public abstract class MixinTextVisitFactory implements Util {

    @Shadow
    private static boolean visitRegularCharacter(Style style, CharacterVisitor visitor, int index, char c) {
        return false;
    }

    /**
     * @param text
     * @return
     */
    @ModifyArg(method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/" +
            "Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value =
            "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;" +
            "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;" +
            "Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), index = 0)
    private static String hookVisitFormatted(String text) {
        if (text == null) {
            return "";
        }
        if (mc.player == null) {
            return text;
        }
        final TextVisitEvent textVisitEvent = new TextVisitEvent(text);
        Util.EVENT_BUS.post(textVisitEvent);
        if (textVisitEvent.isCancelled()) {
            return textVisitEvent.getText();
        }
        return text;
    }
/*
    @Inject(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/" +
            "minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void hookVisitFormatted$1(String text, int startIndex, Style startingStyle, Style resetStyle,
                                             CharacterVisitor visitor, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        int i = text.length();
        Style style = startingStyle;
        for(int j = startIndex; j < i; ++j) {
            char c = text.charAt(j);
            char d;
            if (c == 167) {
                if (j + 1 >= i) {
                    break;
                }

                d = text.charAt(j + 1);
                if (d == 's') { // Custom client color
                    style = style.withColor(Modules.COLORS.getRGB());
                }
                else {
                    Formatting formatting = Formatting.byCode(d);
                    if (formatting != null) {
                        style = formatting == Formatting.RESET ? resetStyle : style.withExclusiveFormatting(formatting);
                    }
                }

                ++j;
            } else if (Character.isHighSurrogate(c)) {
                if (j + 1 >= i) {
                    if (!visitor.accept(j, style, 65533)) {
                        cir.setReturnValue(false);
                        return;
                    }
                    break;
                }

                d = text.charAt(j + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!visitor.accept(j, style, Character.toCodePoint(c, d))) {
                        cir.setReturnValue(false);
                        return;
                    }

                    ++j;
                } else if (!visitor.accept(j, style, 65533)) {
                    cir.setReturnValue(false);
                    return;
                }
            } else if (!visitRegularCharacter(style, visitor, j, c)) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }


 */
}
