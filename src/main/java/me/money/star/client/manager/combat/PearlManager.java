package me.money.star.client.manager.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.util.player.RayCastUtil;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;


public class PearlManager implements Util
{
    private float[] lastThrownAngles;
    private Box pearlBB;

    public PearlManager()
    {
        Util.EVENT_BUS.register(this);
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
      //  if (mc.player == null || !AutoPearl.getInstance().shouldRaytrace())
     //   {
       //     return;
     ////   }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet && lastThrownAngles != null)
        {
            BlockHitResult hitResult = (BlockHitResult) RayCastUtil.rayCast(3.0, lastThrownAngles);
            pearlBB = new Box(hitResult.getPos().subtract(0.4, 0.4, 0.4),
                    hitResult.getPos().add(0.4, 0.4, 0.4));

            if (mc.world.getBlockState(hitResult.getBlockPos()).isAir())
            {
                return;
            }

            if (!pearlBB.contains(packet.change().position().getX(), packet.change().position().getY(), packet.change().position().getZ()))
            {
                event.cancel();
                mc.getNetworkHandler().getConnection().send(new TeleportConfirmC2SPacket(packet.teleportId()));
                mc.getNetworkHandler().getConnection().send(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(),
                        mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false,mc.player.horizontalCollision));
            }
            lastThrownAngles = null;
        }
    }

    public void setLastThrownAngles(float[] lastThrownAngles)
    {
        this.lastThrownAngles = lastThrownAngles;
    }
}
