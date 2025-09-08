package me.money.star.client.manager.player;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY;
import static net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;

public class MovementManager implements Util {
    private boolean packetSneaking;

    public MovementManager()
    {
        Util.EVENT_BUS.register(this);
    }

    /**
     * @param y
     */
    public void setMotionY(double y)
    {
        mc.player.setVelocity(mc.player.getVelocity().getX(), y, mc.player.getVelocity().getZ());
    }

    /**
     * @param x
     * @param z
     */
    public void setMotionXZ(double x, double z)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, z);
    }

    public void setMotionX(double x)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, mc.player.getVelocity().z);
    }

    public void setMotionZ(double z)
    {
        mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, z);
    }


    public void setPacketSneaking(final boolean packetSneaking)
    {
        this.packetSneaking = packetSneaking;
        if (packetSneaking)
        {
            MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(mc.player, PRESS_SHIFT_KEY));
        }
        else
        {
            MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(mc.player, RELEASE_SHIFT_KEY));
        }
    }

}
