package me.money.star.client.manager.anticheat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.util.Arrays;

/**
 * @author xgraza
 * @since 1.0
 */
public final class AntiCheatManager implements Util
{
    private SetbackData lastSetback;

    private final int[] transactions = new int[4];
    private int index;
    private boolean isGrim;

    public AntiCheatManager()
    {
        //Util.EVENT_BUS.register(this);
        Arrays.fill(transactions, -1);
    }

    @Subscribe
    public void onPacketInbound(final PacketEvent event)
    {
        if (event.getPacket() instanceof CommonPingS2CPacket packet)
        {
            if (index > 3)
            {
                return;
            }
            final int uid = packet.getParameter();
            transactions[index] = uid;
            ++index;
            if (index == 4)
            {
                grimCheck();
            }
        }
        else if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet)
        {
            //  lastSetback = new SetbackData(new Vec3d(packet.change().position().x,(packet.change().position().y,(packet.change().position().z),
              //      System.currentTimeMillis(), packet.teleportId()));
        }
    }

    @Subscribe
    public void onDisconnect(final DisconnectEvent event)
    {
        Arrays.fill(transactions, -1);
        index = 0;
        isGrim = false;
    }

    private void grimCheck()
    {
        for (int i = 0; i < 4; ++i)
        {
            if (transactions[i] != -i)
            {
                break;
            }
        }
        isGrim = true;
        MoneyStar.LOGGER.info("Server is running GrimAC.");
    }

    public boolean isGrim()
    {
        return isGrim;
    }

    public boolean hasPassed(final long timeMS)
    {
        return lastSetback != null && lastSetback.timeSince() >= timeMS;
    }
}
