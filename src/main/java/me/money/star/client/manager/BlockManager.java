package me.money.star.client.manager;

import com.google.common.eventbus.Subscribe;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.event.impl.network.PlayerUpdateEvent;
import me.money.star.util.traits.Util;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;


import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class BlockManager implements Util
{
    private final List<BreakEntry> breakPositions = new CopyOnWriteArrayList<>();

    public BlockManager()
    {
        Util.EVENT_BUS.register(this);
    }

    @Subscribe
    public void onTick(PlayerUpdateEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            breakPositions.clear();
            return;
        }

        for (BreakEntry blockEntry : breakPositions)
        {
            //blockEntry.updateDamage();
        }
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet)
        {
            if (countBreaks(packet.getEntityId()) >= 2)
            {
                breakPositions.stream().filter(d -> d.getEntityId() == packet.getEntityId())
                        .min(Comparator.comparingLong(BreakEntry::getStartTime)).ifPresent(breakPositions::remove);
            }
            BreakEntry data = new BreakEntry(packet.getEntityId(), packet.getPos());
           // data.startMining();
            breakPositions.add(data);
        }
    }

    public long countBreaks(int entityId)
    {
        return breakPositions.stream().filter(d -> d.getEntityId() == entityId).count();
    }

    public boolean isInstantMine(BlockPos pos)
    {
        return breakPositions.getFirst().getPos().equals(pos);
    }

    public boolean isBreaking(BlockPos pos)
    {
        return breakPositions.stream().anyMatch(d -> d.getPos().equals(pos));
    }

    public boolean isPassed(BlockPos pos, float blockDamage)
    {
        return breakPositions.stream().anyMatch(d -> d.getPos().equals(pos) && d.getBlockDamage() >= blockDamage);
    }

    public Set<BlockPos> getMines(float blockDamage)
    {
        return breakPositions.stream().filter(d -> isPassed(d.getPos(), blockDamage)).map(BreakEntry::getPos).collect(Collectors.toSet());
    }

    public static class BreakEntry
    {
        private final int entityId;
        private final BlockPos pos;
        private long startTime;
        private float blockDamage;
        private boolean started;

        public BreakEntry(int entityId, BlockPos pos)
        {
            this.entityId = entityId;
            this.pos = pos;
        }
/*
        public void updateDamage()
        {
            if (started)
            {
                blockDamage += Speedmine.getInstance().calcBlockBreakingDelta(
                        mc.world.getBlockState(pos), mc.world, pos);
            }
        }

        public void startMining()
        {
            started = true;
            startTime = System.currentTimeMillis();
        }

 */

        public BlockPos getPos()
        {
            return pos;
        }

        public float getBlockDamage()
        {
            return Math.min(blockDamage, 1.0f);
        }

        public int getEntityId()
        {
            return entityId;
        }

        public long getStartTime()
        {
            return startTime;
        }
    }
}
