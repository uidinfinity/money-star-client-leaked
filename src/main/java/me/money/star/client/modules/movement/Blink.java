package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.util.traits.Util;
import me.money.star.util.world.FakePlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


    public class Blink extends Module {
        public Setting<Boolean> pulse = bool("Pulse", false);

        public Setting<Boolean> render = bool("Render", true);
        public Setting<Float> factor = num("Factor ", 1.0f, 0.0f, 10.0f);


    //
    private FakePlayerEntity serverModel;
    //
    private boolean blinking;
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();

    /**
     *
     */
    public Blink() {
        super("Blink", "Withholds packets from the server, creating clientside lag", Category.MOVEMENT,true,false,false);
    }

    @Override
    public void onEnable() {
        if (render.getValue()) {
            serverModel = new FakePlayerEntity(Util.mc.player, Util.mc.getGameProfile());
            serverModel.despawnPlayer();
            serverModel.spawnPlayer();
        }
    }

    @Override
    public void onDisable() {
        if (Util.mc.player == null) {
            return;
        }
        if (!packets.isEmpty()) {
            for (Packet<?> p : packets) {
                MoneyStar.networkManager.sendPacket(p);
            }
            packets.clear();
        }
        if (serverModel != null) {
            serverModel.despawnPlayer();
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE && pulse.getValue() && packets.size() > factor.getValue() * 10.0f) {
            blinking = true;
            if (!packets.isEmpty()) {
                for (Packet<?> p : packets) {
                    MoneyStar.networkManager.sendPacket(p);
                }
            }
            packets.clear();
            if (serverModel != null) {
                serverModel.copyPositionAndRotation(Util.mc.player);
                serverModel.setHeadYaw(Util.mc.player.headYaw);
            }
            blinking = false;
        }
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        // packets.clear();
        disable();
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent event) {
        if (Util.mc.player == null || Util.mc.player.isRiding() || blinking) {
            return;
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket
                || event.getPacket() instanceof ClientCommandC2SPacket || event.getPacket() instanceof HandSwingC2SPacket
                || event.getPacket() instanceof PlayerInteractEntityC2SPacket || event.getPacket() instanceof PlayerInteractBlockC2SPacket
                || event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            event.cancel();
            packets.add(event.getPacket());
        }
    }


}
