package me.money.star.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import me.money.star.MoneyStar;
import me.money.star.event.impl.PacketEvent;
import me.money.star.client.modules.client.AntiCheat;
import me.money.star.util.traits.Util;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( ClientConnection.class )
public class MixinClientConnection {
    @Shadow @Nullable private volatile PacketListener packetListener;
    @Shadow private Channel channel;
    @Shadow @Final private NetworkSide side;
    @Shadow @Final private static Logger LOGGER;
    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void hookExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (MoneyStar.moduleManager.getModuleByClass(AntiCheat.class).isPacketKick()) {
            LOGGER.error("Exception caught on network thread:", ex);
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext chc, Packet<?> packet, CallbackInfo ci) {
        if (this.channel.isOpen() && packet != null) {
            try {
                PacketEvent.Receive event = new PacketEvent.Receive(packet);
                Util.EVENT_BUS.post(event);
                if (event.isCancelled())
                    ci.cancel();
            } catch (Exception e) {
            }
        }
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (this.side != NetworkSide.CLIENTBOUND) return;
        try {
            PacketEvent.Send event = new PacketEvent.Send(packet);
            Util.EVENT_BUS.post(event);
            if (event.isCancelled()) ci.cancel();
        } catch (Exception e) {
        }
    }
    @Inject(method = "sendImmediately", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks,
                                     boolean flush, CallbackInfo ci) {
        PacketEvent.Outbound packetOutboundEvent =
                new PacketEvent.Outbound(packet);
        Util.EVENT_BUS.post(packetOutboundEvent);
        if (packetOutboundEvent.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * @param channelHandlerContext
     * @param packet
     * @param ci
     */
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;" +
            "Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookChannelRead0(ChannelHandlerContext channelHandlerContext,
                                  Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Inbound packetInboundEvent =
                new PacketEvent.Inbound(packetListener, packet);
        Util.EVENT_BUS.post(packetInboundEvent);
        // prevent client from receiving packet from server
        if (packetInboundEvent.isCancelled()) {
            ci.cancel();
        }
    }


}