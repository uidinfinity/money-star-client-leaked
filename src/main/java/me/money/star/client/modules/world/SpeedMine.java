package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.BlockPlacerModule;
import me.money.star.client.gui.modules.CombatModule;
import me.money.star.client.modules.client.AntiCheat;
import me.money.star.client.modules.combat.AutoFeetPlace;
import me.money.star.client.modules.combat.AutoMine;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.ClientEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.UpdateEvent;
import me.money.star.event.impl.network.AttackBlockEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.mixin.accessor.AccessorClientPlayerInteractionManager;
import me.money.star.util.models.FirstOutQueue;
import me.money.star.util.player.EnchantmentUtil;
import me.money.star.util.player.RotationUtil;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class SpeedMine extends CombatModule
{
    private static SpeedMine INSTANCE;
    public Setting<SpeedmineMode> mode = mode("Mode", SpeedmineMode.PACKET);
    public Setting<Boolean> rotate = bool("Rotate", false);
    public Setting<Boolean> grim = bool("grim", false);
    public Setting<Boolean> grimNew = bool("grimNew", false);
    public Setting<Float> speed = num("Speed", 4.0f,  0.1f,  6.0f);
    public Setting<Float> range = num("Range",  1.0f, 0.1f, 1.0f);
    public Setting<Swap> swap = mode("Swap", Swap.OFF);
    public Setting<Boolean>  doubleBreak = bool("doubleBreak", false);
    public Setting<Boolean>  switchReset = bool("switchReset", false);
    public Setting<Boolean>  instant = bool("instant", false);


    
    private FirstOutQueue<MiningData> miningQueue = new FirstOutQueue<>(2);
    private long lastBreak;
    public SpeedMine()
    {
        super("SpeedMine", "Automatically places blocks below you.", Category.WORLD,true,false,false);
        INSTANCE = this;
    }
    public static SpeedMine getInstance()
    {
        return INSTANCE;
    }


    @Override
    public void onDisable()
    {
        miningQueue.clear();

        MoneyStar.inventoryManager.syncToClient();
    }

    @Override
    public void onEnable()
    {
        if (doubleBreak.getValue())
        {
            miningQueue = new FirstOutQueue<>(2);
        }
        else
        {
            miningQueue = new FirstOutQueue<>(1);
        }
    }

    @Subscribe
    public void onPlayerTick(final TickEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || event.getStage() != Stage.PRE)
        {
            return;
        }

        if (mode.getValue() == SpeedmineMode.DAMAGE)
        {
            AccessorClientPlayerInteractionManager interactionManager =
                    (AccessorClientPlayerInteractionManager) mc.interactionManager;
            if (interactionManager.hookGetCurrentBreakingProgress() >= speed.getValue())
            {
                interactionManager.hookSetCurrentBreakingProgress(1.0f);
            }
            return;
        }

        if (AutoMine.getInstance().isEnabled())
        {
            return;
        }

        if (miningQueue.isEmpty())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (data.getState().isAir())
            {
                data.resetBreakTime();
            }
            if (isDataPacketMine(data) && (data.getState().isAir() || data.hasAttemptedBreak()
                    && data.passedAttemptedBreakTime(500)))
            {
                MoneyStar.inventoryManager.syncToClient();
                miningQueue.remove(data);
                continue;
            }
            final float damageDelta = calcBlockBreakingDelta(data.getState(), mc.world, data.getPos());
            data.damage(damageDelta);
            if (isDataPacketMine(data) && data.getBlockDamage() >= 1.0f && data.getSlot() != -1)
            {
                if (mc.player.isUsingItem() && !multitask.getValue())
                {
                    return;
                }

                if (data.getSlot() != MoneyStar.inventoryManager.getServerSlot())
                {
                    MoneyStar.inventoryManager.setSlot(data.getSlot());
                }
                if (!data.hasAttemptedBreak())
                {
                    data.setAttemptedBreak(true);
                }
            }
        }
        MiningData miningData2 = miningQueue.getFirst();
        final double distance = mc.player.getEyePos().squaredDistanceTo(miningData2.getPos().toCenterPos());
        if (distance > range.getValue())
        {
            // abortMining(miningData);
            miningQueue.remove(miningData2);
            return;
        }
        if (miningData2.getState().isAir())
        {
            return;
        }
        // Something went wrong, remove and remine
        if (miningData2.getBlockDamage() >= speed.getValue() && miningData2.hasAttemptedBreak()
                && miningData2.passedAttemptedBreakTime(500))
        {
            abortMining(miningData2);
            miningQueue.remove(miningData2);
        }
        if (miningData2.getBlockDamage() >= speed.getValue())
        {
            if (mc.player.isUsingItem() && !multitask.getValue())
            {
                return;
            }
            stopMining(miningData2);

            if (!instant.getValue())
            {
                miningQueue.remove(miningData2);
            }

            if (!miningData2.hasAttemptedBreak())
            {
                miningData2.setAttemptedBreak(true);
            }
        }
    }

    @Subscribe
    public void onAttackBlock(final AttackBlockEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || mode.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (AutoMine.getInstance().isEnabled())
        {
            return;
        }
        event.cancel();

        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || event.getState().isAir())
        {
            return;
        }

        startManualMine(event.getPos(), event.getDirection());
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerActionC2SPacket packet
                && packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                && mode.getValue() == SpeedmineMode.DAMAGE && grim.getValue())
        {
            MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(500), packet.getDirection()));
        }

        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchReset.getValue()
                && mode.getValue() == SpeedmineMode.PACKET)
        {
            for (MiningData data : miningQueue)
            {
                data.resetDamage();
            }
        }
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mode.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (AutoMine.getInstance().isEnabled())
        {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            handleBlockUpdatePacket(packet);
        }

        else if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof BlockUpdateS2CPacket packet2)
                {
                    handleBlockUpdatePacket(packet2);
                }
            }
        }
    }

    private void handleBlockUpdatePacket(BlockUpdateS2CPacket packet)
    {
        if (!packet.getState().isAir())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (data.hasAttemptedBreak() && data.getPos().equals(packet.getPos()))
            {
                data.setAttemptedBreak(false);
            }
        }
    }

    @Subscribe
    public void onUpdate(ClientEvent event)
    {
        {
            if (doubleBreak.getValue())
            {
                miningQueue = new FirstOutQueue<>(2);
            }
            else
            {
                miningQueue = new FirstOutQueue<>(1);
            }
        }
    }

   

    private void startManualMine(BlockPos pos, Direction direction)
    {
        clickMine(new MiningData(pos, direction));
    }

    public void clickMine(MiningData miningData)
    {
        int queueSize = miningQueue.size();
        if (queueSize <= 2)
        {
            queueMiningData(miningData);
        }
    }

    private void queueMiningData(MiningData data)
    {
        if (data.getState().isAir())
        {
            return;
        }
        if (startMining(data))
        {
            if (miningQueue.stream().anyMatch(p1 -> data.getPos().equals(p1.getPos())))
            {
                return;
            }
            miningQueue.addFirst(data);
        }
    }

    private boolean startMining(MiningData data)
    {
        if (data.isStarted())
        {
            return false;
        }

        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
        data.setStarted();
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
            return true;
        }

        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        return true;
    }

    private void abortMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir())
        {
            return;
        }
        MoneyStar.networkManager.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        MoneyStar.inventoryManager.syncToClient();
    }

    private void stopMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir())
        {
            return;
        }
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
        int slot = data.getSlot();
        boolean canSwap = slot != -1 && slot != MoneyStar.inventoryManager.getServerSlot();
        if (canSwap)
        {
            swapTo(slot);
        }
        stopMiningInternal(data);
        lastBreak = System.currentTimeMillis();
        if (canSwap)
        {
            swapSync(slot);
        }
        if (rotate.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
        }
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

    private void stopMiningInternal(MiningData data)
    {
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L80
    public boolean isBlockDelayGrim()
    {
        return System.currentTimeMillis() - lastBreak <= 280 && grim.getValue();
    }

    private boolean isDataPacketMine(MiningData data)
    {
        return miningQueue.size() == 2 && data == miningQueue.getLast();
    }

    public float calcBlockBreakingDelta(BlockState state, BlockView world, BlockPos pos)
    {
        if (swap.getValue() == Swap.OFF)
        {
            return state.calcBlockBreakingDelta(mc.player, mc.world, pos);
        }
        float f = state.getHardness(world, pos);
        if (f == -1.0f)
        {
            return 0.0f;
        }
        else
        {
            int i = canHarvest(state) ? 30 : 100;
            return getBlockBreakingSpeed(state) / f / (float) i;
        }
    }

    private float getBlockBreakingSpeed(BlockState block)
    {
        int tool = AutoTool.getInstance().getBestTool(block);
        float f = mc.player.getInventory().getStack(tool).getMiningSpeedMultiplier(block);
        if (f > 1.0F)
        {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            int i = EnchantmentUtil.getLevel(stack, Enchantments.EFFICIENCY);
            if (i > 0 && !stack.isEmpty())
            {
                f += (float) (i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player))
        {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
        {
            float g = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier())
            {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1e-4f;
            };
            f *= g;
        }
//        if (mc.player.isSubmergedIn(FluidTags.WATER) && EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.AQUA_AFFINITY) <= 0)
//        {
//            f /= 5.0f;
//        }
        if (!mc.player.isOnGround())
        {
            f /= 5.0f;
        }
        return f;
    }

    private boolean canHarvest(BlockState state)
    {
        if (state.isToolRequired())
        {
            int tool = AutoTool.getInstance().getBestTool(state);
            return mc.player.getInventory().getStack(tool).isSuitableFor(state);
        }
        return true;
    }

    public boolean isMining()
    {
        return !miningQueue.isEmpty();
    }

    public static class MiningData
    {
        private boolean attemptedBreak;
        private long breakTime;
        private final BlockPos pos;
        private final Direction direction;
        private float lastDamage;
        private float blockDamage;
        private boolean started;

        public MiningData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
        }

        public void setAttemptedBreak(boolean attemptedBreak)
        {
            this.attemptedBreak = attemptedBreak;
            if (attemptedBreak)
            {
                resetBreakTime();
            }
        }

        public void resetBreakTime()
        {
            breakTime = System.currentTimeMillis();
        }

        public boolean hasAttemptedBreak()
        {
            return attemptedBreak;
        }

        public boolean passedAttemptedBreakTime(long time)
        {
            return System.currentTimeMillis() - breakTime >= time;
        }

        public float damage(final float dmg)
        {
            lastDamage = blockDamage;
            blockDamage += dmg;
            return blockDamage;
        }

        public void setDamage(float blockDamage)
        {
            this.blockDamage = blockDamage;
        }

        public void resetDamage()
        {
            started = false;
            blockDamage = 0.0f;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public int getSlot()
        {
            return AutoTool.getInstance().getBestToolNoFallback(getState());
        }

        public BlockState getState()
        {
            return mc.world.getBlockState(pos);
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }

        public boolean isStarted()
        {
            return started;
        }

        public void setStarted()
        {
            this.started = true;
        }
    }

    public enum SpeedmineMode
    {
        PACKET,
        DAMAGE
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }
}