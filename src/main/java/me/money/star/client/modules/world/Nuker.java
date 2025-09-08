package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.CombatModule;
import me.money.star.client.modules.client.AntiCheat;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.network.AttackBlockEvent;
import me.money.star.event.impl.network.PacketEvent;

import me.money.star.util.player.RotationUtil;
import me.money.star.util.world.BlastResistantBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;

import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;


import java.util.*;
import java.util.List;
public final class Nuker extends CombatModule
{
    private static Nuker INSTANCE;
    public Setting<NukeMode> mode = mode("Mode", NukeMode.SELECT);
    public Setting<Boolean>  strictDirection = bool("StrictDirection", false);
    public Setting<Boolean>  flatten = bool("Flatten", false);
    public Setting<Boolean> rotate = bool("Rotate", false);
    public Setting<Boolean> grim = bool("grim", false);
    public Setting<Boolean> grimNew = bool("grimNew", false);
    public Setting<Float> speed = num("Speed", 4.0f,  0.1f,  6.0f);
    public Setting<Float> range = num("Range",  1.0f, 0.1f, 1.0f);
    public Setting<Integer> mineTicks = num("MiningTicks", 20,  5,  60);
    public Setting<Swap> swap = mode("Swap", Swap.OFF);
    public Setting<Boolean> swapBefore = bool("SwapBefore", false);
    public Setting<Boolean>  doubleBreak = bool("doubleBreak", false);
    public Setting<Boolean>  switchReset = bool("switchReset", false);




    private MineData packetMine, instantMine; // mining2 should always be the instant mine
    private boolean packetSwapBack;
    private boolean changedInstantMine;
    private boolean waitForPacketMine;

    private final Queue<BlockPos> selectedBlocks = new LinkedList<>();
    private final Queue<MineData> autoMineQueue = new ArrayDeque<>();
    private int autoMineTickDelay;
    public Nuker()
    {
        super("Nuker", "Automatically places blocks below you.", Category.WORLD,true,false,false);
        INSTANCE = this;
    }
    public static Nuker getInstance()
    {
        return INSTANCE;
    }


    @Override
    public void onDisable()
    {
        autoMineQueue.clear();
        packetMine = null;
        if (instantMine != null)
        {
            abortMining(instantMine);
            instantMine = null;
        }

        autoMineTickDelay = 0;
        waitForPacketMine = false;
        if (packetSwapBack)
        {
            MoneyStar.inventoryManager.syncToClient();
            packetSwapBack = false;
        }
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || event.getStage() != Stage.PRE)
        {
            return;
        }

        if (isInstantMineComplete())
        {
            if (changedInstantMine)
            {
                changedInstantMine = false;
            }
            if (waitForPacketMine)
            {
                waitForPacketMine = false;
            }
        }

        autoMineTickDelay--;

        // Mining packet handling
        if (packetMine != null && packetMine.getTicksMining() > mineTicks.getValue())
        {
            
            if (packetSwapBack)
            {
                MoneyStar.inventoryManager.syncToClient();
                packetSwapBack = false;
            }
            selectedBlocks.remove(packetMine.getPos());
            packetMine = null;
            if (!isInstantMineComplete())
            {
                waitForPacketMine = true;
            }
        }

        if (packetMine != null)
        {
            final float damageDelta = SpeedMine.getInstance().calcBlockBreakingDelta(
                    packetMine.getState(), mc.world, packetMine.getPos());
            packetMine.addBlockDamage(damageDelta);

            int slot = packetMine.getBestSlot();
            float damageDone = packetMine.getBlockDamage() + (swapBefore.getValue() ? damageDelta : 0.0f);
            if (damageDone >= 1.0f && canMine(packetMine.getState()) && slot != -1 && !checkMultitask())
            {
                packetMine.markAttemptedMine();
                MoneyStar.inventoryManager.setSlot(slot);
                packetSwapBack = true;
            }
        }

        if (packetSwapBack && (packetMine == null || !canMine(packetMine.getState())))
        {
            MoneyStar.inventoryManager.syncToClient();
            packetSwapBack = false;
            
            if (packetMine != null)
            {
                selectedBlocks.remove(packetMine.getPos());
            }
            packetMine = null;
            if (!isInstantMineComplete())
            {
                waitForPacketMine = true;
            }
        }

        if (instantMine != null)
        {
            final double distance = mc.player.getEyePos().squaredDistanceTo(instantMine.getPos().toCenterPos());
            if (distance > range.getValue()
                    || instantMine.getTicksMining() > mineTicks.getValue())
            {
                abortMining(instantMine);
                
                selectedBlocks.remove(instantMine.getPos());
                instantMine = null;
            }
        }

        if (instantMine != null)
        {
            final float damageDelta = SpeedMine.getInstance().calcBlockBreakingDelta(
                    instantMine.getState(), mc.world, instantMine.getPos());
            instantMine.addBlockDamage(damageDelta);

            if (instantMine.getBlockDamage() >= speed.getValue())
            {
                if (canMine(instantMine.getState()))
                {
                    if (!checkMultitask() || multitask.getValue() || swap.getValue() == Swap.OFF)
                    {
                        stopMining(instantMine);
                        instantMine.markAttemptedMine();
                    }
                }
                else
                {
                    abortMining(instantMine);
                    
                    selectedBlocks.remove(instantMine.getPos());
                    instantMine = null;
                }
            }
        }

        if (!autoMineQueue.isEmpty() && autoMineTickDelay <= 0)
        {
            MineData nextMine = autoMineQueue.poll();
            if (nextMine != null)
            {
                startMining(nextMine);
                autoMineTickDelay = 5;
            }
        }

        if (autoMineQueue.isEmpty())
        {
            MineData bestMine = getNukerMine();
            if (bestMine != null && (packetMine == null
                    && doubleBreak.getValue() || isInstantMineComplete()))
            {
                startAutoMine(bestMine);
            }
        }
    }

    @Subscribe
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }

        event.cancel();

        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || !canMine(event.getState())
                || selectedBlocks.contains(event.getPos()))
        {
            return;
        }

        final double distance = mc.player.getEyePos().squaredDistanceTo(event.getPos().toCenterPos());
        if (distance > range.getValue())
        {
            return;
        }

        selectedBlocks.add(event.getPos());
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchReset.getValue())
        {
            instantMine.setTotalBlockDamage(0.0f, 0.0f);
        }
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && !canMine(packet.getState())
                && packetMine != null && packetMine.getPos().equals(packet.getPos()))
        {
            
            if (packetSwapBack)
            {
                MoneyStar.inventoryManager.syncToClient();
                packetSwapBack = false;
            }
            packetMine = null;
            waitForPacketMine = false;
            if (!isInstantMineComplete())
            {
                waitForPacketMine = true;
            }
        }
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(range.getValue());
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = flatten.getValue() ? 0.0 : -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    public void startAutoMine(MineData data)
    {
        if (!canMine(data.getState()) || isMining(data.getPos()))
        {
            return;
        }

        if (!doubleBreak.getValue())
        {
            instantMine = data;
            autoMineQueue.offer(data);
            return;
        }

        if (changedInstantMine && !isInstantMineComplete() || waitForPacketMine)
        {
            return;
        }

        boolean updateChanged = false;
        if (!isInstantMineComplete() && !changedInstantMine)
        {
            if (packetMine == null)
            {
                packetMine = instantMine.copy();

            }
            else
            {
                updateChanged = true;
            }
        }

        instantMine = data;
        autoMineQueue.offer(data);

        if (updateChanged)
        {
            changedInstantMine = true;
        }
    }





    // Should be sorted by y level and distance
    public MineData getNukerMine()
    {
        if (mode.getValue() == NukeMode.SPHERE)
        {
            List<BlockPos> sphere = getSphere(mc.player.getPos());

            BlockPos minePos = null;
            int yLevel = -128;
            double dist = Double.MAX_VALUE;
            for (BlockPos blockPos : sphere)
            {
                BlockState state = mc.world.getBlockState(blockPos);
                if (!canMine(state) || isMining(blockPos))
                {
                    continue;
                }

                final double distance = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
                if (distance > range.getValue())
                {
                    continue;
                }

                int y = blockPos.getY();
                double distToPlayer = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());

                if (y > yLevel || (y == yLevel && distToPlayer < dist))
                {
                    minePos = blockPos;
                    yLevel = y;
                    dist = distToPlayer;
                }
            }

            if (minePos != null)
            {
                return new MineData(minePos, strictDirection.getValue() ?
                        MoneyStar.interactionManager.getInteractDirection(minePos, false) : Direction.UP);
            }
        }

        else
        {
            if (selectedBlocks.isEmpty())
            {
                return null;
            }

            if (packetMine != null && instantMine != null)
            {
                return null;
            }

            Queue<BlockPos> blocks = new LinkedList<>(selectedBlocks);
            BlockPos minePos = blocks.poll();
            if (instantMine != null && instantMine.getPos().equals(minePos))
            {
                minePos = selectedBlocks.poll();
            }
            if (minePos != null)
            {
                return new MineData(minePos, strictDirection.getValue() ?
                        MoneyStar.interactionManager.getInteractDirection(minePos, false) : Direction.UP);
            }
        }

        return null;
    }

    public void startMining(MineData data)
    {
        if (doubleBreak.getValue())
        {
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
            if (grimNew.getValue())
            {
                if (!AntiCheat.getInstance().getMiningFix())
                {
                    MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                    MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                    MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                }
                else
                {
                    MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                }

                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            else
            {
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
        else
        {
            MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            if (!grim.getValue())
            {
                MoneyStar.networkManager.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            }
        }


    }

    public void abortMining(MineData data)
    {
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    public void stopMining(MineData data)
    {
        if (rotate.getValue())
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            if (grim.getValue())
            {
                setRotationSilent(rotations[0], rotations[1]);
            }
            else
            {
                setRotation(rotations[0], rotations[1]);
            }
        }
        int slot = data.getBestSlot();
        boolean canSwap = slot != -1 && slot != MoneyStar.inventoryManager.getServerSlot();
        if (canSwap)
        {
            swapTo(slot);
        }

        stopMiningInternal(data);

        if (canSwap)
        {
            swapSync(slot);
        }

        if (rotate.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
        }
    }

    private void stopMiningInternal(MineData data)
    {
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    public boolean isInstantMineComplete()
    {
        return instantMine == null || instantMine.getBlockDamage() >= speed.getValue() && !canMine(instantMine.getState());
    }

    private void swapTo(int slot)
    {
        switch (swap.getValue())
        {
            case NORMAL -> MoneyStar.inventoryManager.setClientSlot(slot);
            case SILENT -> MoneyStar.inventoryManager.setSlot(slot);
            case SILENT_ALT -> MoneyStar.inventoryManager.setSlotAlt(slot);
        }
    }

    private void swapSync(int slot)
    {
        switch (swap.getValue())
        {
            case SILENT -> MoneyStar.inventoryManager.syncToClient();
            case SILENT_ALT -> MoneyStar.inventoryManager.setSlotAlt(slot);
        }
    }

    private boolean isMining(BlockPos blockPos)
    {
        return instantMine != null && instantMine.getPos().equals(blockPos) ||
                packetMine != null && packetMine.getPos().equals(blockPos);
    }

    private boolean validNukerBlock(Block block)
    {
        if (BlastResistantBlocks.isUnbreakable(block))
        {
            return false;
        }
        return true;
    }

    public boolean canMine(BlockState state)
    {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    public static class MineData
    {
        private final BlockPos pos;
        private final Direction direction;
        //
        private int ticksMining;
        private float blockDamage, lastDamage;

        public MineData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof MineData d && d.getPos().equals(pos);
        }

        public void resetMiningTicks()
        {
            ticksMining = 0;
        }

        public void markAttemptedMine()
        {
            ticksMining++;
        }

        public void addBlockDamage(float blockDamage)
        {
            this.lastDamage = this.blockDamage;
            this.blockDamage += blockDamage;
        }

        public void setTotalBlockDamage(float blockDamage, float lastDamage)
        {
            this.blockDamage = blockDamage;
            this.lastDamage = lastDamage;
        }

        public static MineData empty()
        {
            return new MineData(BlockPos.ORIGIN, Direction.UP);
        }

        public MineData copy()
        {
            final MineData data = new MineData(pos, direction);
            data.setTotalBlockDamage(blockDamage, lastDamage);
            return data;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public int getTicksMining()
        {
            return ticksMining;
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }

        public BlockState getState()
        {
            return mc.world.getBlockState(pos);
        }

        public int getBestSlot()
        {
            return AutoTool.getInstance().getBestToolNoFallback(getState());
        }
    }


    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    public enum NukeMode
    {
        SPHERE,
        SELECT
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }
}