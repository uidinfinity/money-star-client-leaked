package me.money.star.mixin;

import me.money.star.event.impl.ChatEvent;
import me.money.star.event.impl.ServerRotationEvent;
import me.money.star.event.impl.gui.chat.ChatMessageEvent;
import me.money.star.event.impl.network.GameJoinEvent;
import me.money.star.event.impl.network.InventoryEvent;
import me.money.star.mixin.accessor.AccessorClientConnection;
import me.money.star.util.traits.IClientPlayNetworkHandler;
import me.money.star.util.traits.Util;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.money.star.util.traits.Util.mc;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler {
    @Shadow
    public abstract ClientConnection getConnection();

    /**
     * @param content
     * @param ci
     */
    @Inject(method = "sendChatMessage", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendChatMessage(String content, CallbackInfo ci) {
        ChatMessageEvent.Server chatInputEvent =
                new ChatMessageEvent.Server(content);
        Util.EVENT_BUS.post(chatInputEvent);
        // prevent chat packet from sending
        if (chatInputEvent.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * @param packet
     * @param ci
     */
    @Inject(method = "onGameJoin", at = @At(value = "TAIL"))
    private void hookOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        GameJoinEvent gameJoinEvent = new GameJoinEvent();
        Util.EVENT_BUS.post(gameJoinEvent);
    }

    /**
     * @param packet
     * @param ci
     */
    @Inject(method = "onInventory", at = @At(value = "TAIL"))
    private void hookOnInventory(InventoryS2CPacket packet, CallbackInfo ci) {
        InventoryEvent inventoryEvent = new InventoryEvent(packet);
        Util.EVENT_BUS.post(inventoryEvent);
    }

    @Override
    public void sendQuietPacket(Packet<?> packet) {
        ((AccessorClientConnection) getConnection()).hookSendInternal(packet, null, true);
    }
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(String content, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(content);
        Util.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }


}
