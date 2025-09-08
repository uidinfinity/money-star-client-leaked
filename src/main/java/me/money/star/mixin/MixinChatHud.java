package me.money.star.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.money.star.client.manager.RenderManager;
import me.money.star.event.impl.*;
import me.money.star.mixin.imixin.IChatHudLine;
//import me.money.star.mixin.imixin.IChatHudLineVisible;
import me.money.star.util.chat.FormattingUtil;
import me.money.star.util.traits.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import me.money.star.mixin.imixin.IChatHud;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHud, Util
{
    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    public abstract double getChatScale();

    @Shadow
    public abstract int getWidth();

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private boolean hasUnreadNewMessages;

    @Shadow
    public abstract void scroll(int scroll);

    @Shadow
    private int scrolledLines;

    @Shadow
    public abstract boolean isChatFocused();

    @Shadow
    public abstract void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator);

    @Unique
    private ChatHudLine.Visible current = null;

    @Unique
    private int currentId;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"))
    private void hookTimeAdded(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex)
    {
        try
        {
            current = visibleMessages.get(chatLineIndex);
        }
        catch (Exception ignored)
        {

        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;" +
                            "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;" +
                            "Lnet/minecraft/text/OrderedText;III)I"))
    private int drawTextWithShadowHook(DrawContext instance,
                                       TextRenderer textRenderer,
                                       OrderedText text,
                                       int x,
                                       int y,
                                       int color)
    {
        RenderChatHudEvent renderChatHudEvent = new RenderChatHudEvent(current);
        Util.EVENT_BUS.post(renderChatHudEvent);

        ChatTextRenderEvent chatTextRenderEvent = new ChatTextRenderEvent();
        Util.EVENT_BUS.post(chatTextRenderEvent);

        if (renderChatHudEvent.isCancelled())
        {
            if (renderChatHudEvent.getAnimationMode())
            {
                if (chatTextRenderEvent.isCancelled())
                {
                    RenderManager.renderText(instance, FormattingUtil.toString(text), (int) renderChatHudEvent.getAnimation(), y, color);
                    return RenderManager.textWidth(FormattingUtil.toString(text));
                }
                return instance.drawTextWithShadow(textRenderer, text, (int) renderChatHudEvent.getAnimation(), y, color);
            }
            else
            {
                float alpha = (float) MathHelper.clamp(renderChatHudEvent.getAnimation(), 0.0f, 1.0f);

                float colorAlpha = (color >> 24) & 0xFF;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
                int colorAlphaInt = Math.max(10, (int) (colorAlpha * alpha));

                int color1 = alpha == 1.0f ? color : (colorAlphaInt << 24) | (color & 0xFFFFFF);
                if (chatTextRenderEvent.isCancelled())
                {
                    RenderManager.renderText(instance, FormattingUtil.toString(text), 0, y, color1);
                    return RenderManager.textWidth(FormattingUtil.toString(text));
                }
                return instance.drawTextWithShadow(textRenderer, text, 0, y, color1);
            }
        }
        if (chatTextRenderEvent.isCancelled())
        {
            RenderManager.renderText(instance, FormattingUtil.toString(text), 0, y, color);
            return RenderManager.textWidth(FormattingUtil.toString(text));
        }
        return instance.drawTextWithShadow(textRenderer, text, 0, y, color);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator hookRender(MessageIndicator original)
    {
        SignatureIndicatorEvent signatureIndicatorEvent = new SignatureIndicatorEvent();
        Util.EVENT_BUS.post(signatureIndicatorEvent);
        return signatureIndicatorEvent.isCancelled() ? null : original;
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci)
    {
        ci.cancel();
        try
        {
            //visibleMessages.removeIf(msg -> ((IChatHudLineVisible) (Object) msg).getId() == currentId && currentId != 0);
            for (int i = messages.size() - 1; i > -1; i--)
            {
                if (((IChatHudLine) (Object) messages.get(i)).getId() == currentId && currentId != 0)
                {
                    messages.remove(i);
                }
            }
            ChatMessageHudEvent chatTextEvent = new ChatMessageHudEvent(message);
            Util.EVENT_BUS.post(chatTextEvent);
            int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
            if (indicator != null && indicator.icon() != null)
            {
                i -= indicator.icon().width + 4 + 2;
            }
            List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(chatTextEvent.isCancelled() ? chatTextEvent.getText() : message, i, this.client.textRenderer);
            boolean bl = this.isChatFocused();
            for (int j = 0; j < list.size(); ++j)
            {
                OrderedText orderedText = list.get(j);
                if (bl && this.scrolledLines > 0)
                {
                    this.hasUnreadNewMessages = true;
                    this.scroll(1);
                }
                ChatTextEvent chatMessageEvent = new ChatTextEvent(orderedText);
                Util.EVENT_BUS.post(chatMessageEvent);
                boolean bl2 = j == list.size() - 1;
                ChatHudLine.Visible visibleLine = new ChatHudLine.Visible(mc.inGameHud.getTicks(), chatMessageEvent.isCancelled() ? chatMessageEvent.getText() : orderedText, indicator, bl2);
               // ((IChatHudLineVisible) (Object) visibleLine).setId(currentId);
                this.visibleMessages.add(0, visibleLine);
            }
            ChatLengthEvent chatLengthEvent = new ChatLengthEvent();
            Util.EVENT_BUS.post(chatLengthEvent);
            boolean bl1 = chatLengthEvent.isCancelled();
            if (!bl1)
            {
                while (this.visibleMessages.size() > 100)
                {
                    this.visibleMessages.remove(this.visibleMessages.size() - 1);
                }
            }
        }
        catch (Exception ignored)
        {

        }
    }

    @Inject(method = "clear", at = @At(value = "HEAD"), cancellable = true)
    private void hookClear(boolean clearHistory, CallbackInfo ci)
    {
        ChatHistoryEvent chatHistoryEvent = new ChatHistoryEvent();
        Util.EVENT_BUS.post(chatHistoryEvent);
        if (chatHistoryEvent.isCancelled())
        {
            ci.cancel();
        }
    }

    @Override
    public void addMessage(Text message, MessageIndicator messageIndicator, int id)
    {
        currentId = id;
        addMessage(message, null, messageIndicator);
        currentId = 0;
    }
}
