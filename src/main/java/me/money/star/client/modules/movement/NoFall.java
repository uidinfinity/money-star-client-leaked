package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.network.PlayerUpdateEvent;
import me.money.star.mixin.accessor.AccessorPlayerMoveC2SPacket;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.world.World;


public class NoFall extends Module {


    public NoFall() {
        super("NoFall", "Prevents all fall damage", Category.MOVEMENT,true,false,false);
    }

    public Setting<NoFallMode> mode = mode("Mode", NoFallMode.PACKET);

    @Subscribe
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (event.getStage() != Stage.PRE || !checkFalling()) {
            return;
        }
        if (mode.getValue() == NoFallMode.LATENCY) {
            if (Util.mc.world.getRegistryKey() == World.NETHER) {
                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        Util.mc.player.getX(), 0, Util.mc.player.getZ(), true, Util.mc.player.horizontalCollision));
            } else {
                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, 64, 0, true, Util.mc.player.horizontalCollision));
            }
            Util.mc.player.fallDistance = 0.0f;
        } else if (mode.getValue() == NoFallMode.GRIM) {
            MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.Full(Util.mc.player.getX(), Util.mc.player.getY() + 1.0e-9,
                    Util.mc.player.getZ(), Util.mc.player.getYaw(), Util.mc.player.getPitch(), true, Util.mc.player.horizontalCollision));
            Util.mc.player.onLanding();
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (Util.mc.player == null || !checkFalling()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (mode.getValue() == NoFallMode.PACKET) {
                ((AccessorPlayerMoveC2SPacket) packet).hookSetOnGround(true);
            } else if (mode.getValue() == NoFallMode.ANTI) {
                double y = packet.getY(Util.mc.player.getY());
                ((AccessorPlayerMoveC2SPacket) packet).hookSetY(y + 0.10000000149011612);
            }
        }
    }
    @Override
    public void onUpdate() {
        if(mode.getValue() == NoFallMode.OYVEY) {
            if (!Util.mc.player.isOnGround() && MoneyStar.positionManager.getFallDistance() > 3) {
                boolean bl = Util.mc.player.horizontalCollision;
                PlayerMoveC2SPacket.Full pakcet = new PlayerMoveC2SPacket.Full(Util.mc.player.getX(), Util.mc.player.getY() + 0.000000001, Util.mc.player.getZ(),
                        Util.mc.player.getYaw(), Util.mc.player.getPitch(), false, bl);
                Util.mc.player.networkHandler.sendPacket(pakcet);

            }
        }
    }

    private boolean checkFalling() {
        return Util.mc.player.fallDistance > Util.mc.player.getSafeFallDistance() && !Util.mc.player.isOnGround()
                && !Util.mc.player.isGliding() && !MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled();
    }

    public enum NoFallMode {
        ANTI,
        OYVEY,
        LATENCY,
        PACKET,
        GRIM
    }
}
