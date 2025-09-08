package me.money.star.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.money.star.event.impl.PlayerListColumnsEvent;
import me.money.star.event.impl.PlayerListIconEvent;
import me.money.star.event.impl.gui.hud.PlayerListEvent;
import me.money.star.event.impl.gui.hud.PlayerListNameEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

/**
 * @author linus, hockeyl8
 * @since 1.0
 */
@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud
{
    @Shadow
    @Final
    private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract List<PlayerListEntry> collectPlayerEntries();

    @Shadow
    protected abstract Text applyGameModeFormatting(PlayerListEntry entry, MutableText name);

    @Inject(method = "getPlayerName", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir)
    {
        Text text;
        if (entry.getDisplayName() != null)
        {
            text = applyGameModeFormatting(entry, entry.getDisplayName().copy());
        }
        else
        {
            text = applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())));
        }
        PlayerListNameEvent playerListNameEvent = new PlayerListNameEvent(text, entry.getProfile().getId());
        Util.EVENT_BUS.post(playerListNameEvent);
        if (playerListNameEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(playerListNameEvent.getPlayerName());
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I"))
    private int hookRender(TextRenderer instance, StringVisitable text)
    {
        PlayerListIconEvent.Width playerListIconEvent = new PlayerListIconEvent.Width(text.getString());
        Util.EVENT_BUS.post(playerListIconEvent);
        if (playerListIconEvent.isCancelled())
        {
            return instance.getWidth(text) + 12;
        }
        return instance.getWidth(text);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
    private int hookRender(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color)
    {
        PlayerListIconEvent.Render playerListIconEvent = new PlayerListIconEvent.Render(text, instance, textRenderer, x, y, color);
        Util.EVENT_BUS.post(playerListIconEvent);
        int x1 = playerListIconEvent.isCancelled() ? x + 12 : x;
        instance.drawTextWithShadow(textRenderer, text, x1, y, color);
        return x1;
    }

    /**
     * @param cir
     */
    @Inject(method = "collectPlayerEntries", at = @At(value = "HEAD"), cancellable = true)
    private void hookCollectPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> cir)
    {
        PlayerListEvent playerListEvent = new PlayerListEvent();
        Util.EVENT_BUS.post(playerListEvent);
        if (playerListEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(client.player.networkHandler.getListedPlayerListEntries()
                    .stream().sorted(ENTRY_ORDERING).limit(playerListEvent.getSize()).toList());
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", shift = At.Shift.BEFORE))
    private void hookRender(CallbackInfo ci, @Local(ordinal = 5) LocalIntRef o, @Local(ordinal = 6) LocalIntRef p)
    {
        int newO;
        int newP = 1;
        int totalPlayers = newO = this.collectPlayerEntries().size();

        PlayerListColumnsEvent playerListColumsEvent = new PlayerListColumnsEvent();
        Util.EVENT_BUS.post(playerListColumsEvent);
        if (playerListColumsEvent.isCancelled())
        {
            while (newO > playerListColumsEvent.getTabHeight())
            {
                newO = (totalPlayers + ++newP - 1) / newP;
            }

            o.set(newO);
            p.set(newP);
        }
    }
}
