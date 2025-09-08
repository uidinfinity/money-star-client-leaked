package me.money.star.event.impl;

import me.money.star.MoneyStar;
import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

public abstract class PacketEvent extends Event {

    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }
    @Cancelable
    public static class Inbound extends me.money.star.event.impl.network.PacketEvent {

        private final PacketListener packetListener;

        /**
         * @param packet
         */
        public Inbound(PacketListener packetListener, Packet<?> packet) {
            super(packet);
            this.packetListener = packetListener;
        }

        public PacketListener getPacketListener() {
            return packetListener;
        }
    }

    /**
     *
     */
    @Cancelable
    public static class Outbound extends PacketEvent {
        //
        private final boolean cached;

        /**
         * @param packet
         */
        public Outbound(Packet<?> packet) {
            super(packet);
            this.cached = MoneyStar.networkManager.isCached(packet);
        }

        /**
         * @return
         */
        public boolean isClientPacket() {
            return cached;
        }
    }
}