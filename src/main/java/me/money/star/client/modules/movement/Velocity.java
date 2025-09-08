package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.entity.player.PushEntityEvent;
import me.money.star.event.impl.entity.player.PushFluidsEvent;
import me.money.star.event.impl.network.PushOutOfBlocksEvent;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class Velocity extends Module {
    public Setting<Boolean> pushEntities = bool("NoPush-Entities", false);
    public Setting<Boolean> pushBlocks = bool("NoPush-Blocks", false);
    public Setting<Boolean> pushLiquids = bool("NoPush-Liquids", false);
    public Velocity() {
        super("Velocity", "You cannot be pushed or thrown back.", Category.MOVEMENT, true, false, false);
    }

    @Subscribe private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket || event.getPacket() instanceof ExplosionS2CPacket) event.cancel();
    }
    @Subscribe
    public void onPushEntity(PushEntityEvent event) {
        if (pushEntities.getValue() && event.getPushed().equals(mc.player)) {
            event.cancel();
        }
    }

    @Subscribe
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
        if (pushBlocks.getValue()) {
            event.cancel();
        }
    }

    @Subscribe
    public void onPushFluid(PushFluidsEvent event) {
        if (pushLiquids.getValue()) {
            event.cancel();
        }
    }
}
