package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.CombatModule;
import me.money.star.client.gui.modules.RotationModule;
import me.money.star.client.modules.client.AntiCheat;
import me.money.star.client.modules.world.AutoTool;
import me.money.star.client.modules.world.SpeedMine;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.network.AttackBlockEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.util.math.position.PositionUtil;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.RotationUtil;
import me.money.star.util.world.BlastResistantBlocks;
import me.money.star.util.world.EntityUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
public class AutoMine extends CombatModule {
    public Setting<Boolean> auto = bool("Auto", true);
    public Setting<Boolean> strictDirection = bool("StrictDirection", false);
    public Setting<Boolean> avoidSelf = bool("AvoidSelf", false);
    public Setting<Float> enemyRange = num("EnemyRange",  5.0f, 1.0f, 10.0f);
    public Setting<Boolean> antiCrawl = bool("AntiCrawl", false);
    public Setting<Boolean> head = bool("TargetBody", false);
    public Setting<Boolean> aboveHead = bool("TargetHead", false);
    public Setting<Boolean> doubleBreak = bool("DoubleBreak", false);
    public Setting<Integer> mineTicks = num("MiningTicks", 20,  5,  60);
    public Setting<RemineMode> remine = mode("Remine", RemineMode.NORMAL);
    public Setting<Boolean> packetInstant = bool("Fast", false);
    public Setting<Float> range = num("Range",4.0f,  0.1f,  6.0f);
    public Setting<Float> speed = num("Speed",  1.0f, 0.1f, 1.0f);
    public Setting<Swap> swap = mode("Swap", Swap.OFF);
    public Setting<Boolean> swapBefore = bool("SwapBefore", false);
    public Setting<Boolean> rotate = bool("Rotate", false);
    public Setting<Boolean> switchReset = bool("SwitchReset", false);
    public Setting<Boolean> grim = bool("Grim", false);
    public Setting<Boolean> grimNew = bool("GrimNew", false);
    private PlayerEntity playerTarget;
    private MineData packetMine, instantMine; // mining2 should always be the instant mine
    private boolean packetSwapBack;
    private boolean manualOverride;
    private final Timer remineTimer = new CacheTimer();

    private boolean changedInstantMine;
    private boolean waitForPacketMine;
    private boolean packetMineStuck;

    private boolean antiCrawlOverride;
    private int antiCrawlTicks;

    private final Queue<MineData> autoMineQueue = new ArrayDeque<>();
    private int autoMineTickDelay;
    private static AutoMine INSTANCE;
    public AutoMine() {
        super("AutoMine", "Automatically aims charged bow at nearby entities",
                Category.COMBAT,true,false,false);
        INSTANCE = this;
    }
    public static AutoMine getInstance()
    {
        return INSTANCE;
    }
    @Override
    public void onDisable()
    {
        autoMineQueue.clear();
        playerTarget = null;
        packetMine = null;
        if (instantMine != null)
        {
            abortMining(instantMine);
            instantMine = null;
        }
        autoMineTickDelay = 0;
        antiCrawlTicks = 0;
        manualOverride = false;
        antiCrawlOverride = false;
        waitForPacketMine = false;
        packetMineStuck = false;
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

        PlayerEntity currentTarget = getClosestPlayer(enemyRange.getValue());
        boolean targetChanged = playerTarget != null && playerTarget != currentTarget;
        playerTarget = currentTarget;

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
        antiCrawlTicks--;

        // Mining packet handling
        if (packetMine != null && packetMine.getTicksMining() > mineTicks.getValue())
        {
            packetMineStuck = true;

            if (packetSwapBack)
            {
                MoneyStar.inventoryManager.syncToClient();
                packetSwapBack = false;
            }
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
            float damageDone = packetMine.getBlockDamage() + (swapBefore.getValue()
                    || packetMineStuck ? damageDelta : 0.0f);
            if (damageDone >= 1.0f && slot != -1  && !checkMultitask())
            {
                MoneyStar.inventoryManager.setSlot(slot);
                packetSwapBack = true;
                if (packetMineStuck)
                {
                    packetMineStuck = false;
                }
            }
        }

        if (packetSwapBack)
        {
            if (packetMine != null && canMine(packetMine.getState()))
            {
                packetMine.markAttemptedMine();
            }
            else
            {
                MoneyStar.inventoryManager.syncToClient();
                packetSwapBack = false;

                packetMine = null;
                if (!isInstantMineComplete())
                {
                    waitForPacketMine = true;
                }
            }
        }

        if (instantMine != null)
        {
            final double distance = mc.player.getEyePos().squaredDistanceTo(instantMine.getPos().toCenterPos());
            if (distance >  range.getValue()
                    || instantMine.getTicksMining() > mineTicks.getValue())
            {
                abortMining(instantMine);

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
                boolean canMine = canMine(instantMine.getState());
                boolean canPlace = mc.world.canPlace(instantMine.getState(), instantMine.getPos(), ShapeContext.absent());
                if (canMine)
                {
                    instantMine.markAttemptedMine();
                }
                else
                {
                    instantMine.resetMiningTicks();
                    if (remine.getValue() == RemineMode.NORMAL || remine.getValue() == RemineMode.FAST)
                    {
                        instantMine.setTotalBlockDamage(0.0f, 0.0f);
                    }

                    if (manualOverride)
                    {
                        manualOverride = false;
                        // Clear our old manual mine
                        abortMining(instantMine);

                        instantMine = null;
                    }
                }

                boolean passedRemine = remine.getValue() == RemineMode.INSTANT || remineTimer.passed(500);
                if (instantMine != null && (remine.getValue() == RemineMode.INSTANT
                        && packetInstant.getValue() && packetMine == null && canPlace || canMine && passedRemine)
                        && (!checkMultitask() || multitask.getValue() || swap.getValue() == Swap.OFF))
                {
                    stopMining(instantMine);
                    remineTimer.reset();

                    if (AutoCrystal.getInstance().isEnabled()
                            && AutoCrystal.getInstance().shouldPreForcePlace())
                    {
                        AutoCrystal.getInstance().placeCrystalForTarget(playerTarget, instantMine.getPos().down());
                    }

                    if (remine.getValue() == RemineMode.FAST)
                    {
                        startMining(instantMine);
                    }
                }
            }
        }

        // Clear overrides
        if (manualOverride && (instantMine == null || instantMine.getGoal() != MiningGoal.MANUAL))
        {
            manualOverride = false;
        }

        if (antiCrawlOverride && (instantMine == null || instantMine.getGoal() != MiningGoal.PREVENT_CRAWL))
        {
            antiCrawlOverride = false;
        }

        if (auto.getValue())
        {
            if (!autoMineQueue.isEmpty() && autoMineTickDelay <= 0)
            {
                MineData nextMine = autoMineQueue.poll();
                if (nextMine != null)
                {
                    startMining(nextMine);
                    autoMineTickDelay = 5;
                }
            }

            BlockPos antiCrawlPos = getAntiCrawlPos(playerTarget);
            if (antiCrawlOverride)
            {
                if (mc.player.getPose().equals(EntityPose.SWIMMING))
                {
                    antiCrawlTicks = 10;
                }

                if (antiCrawlTicks <= 0 || !isInstantMineComplete() && antiCrawlPos != null
                        && !instantMine.getPos().equals(antiCrawlPos))
                {
                    antiCrawlOverride = false;
                }
            }

            if (autoMineQueue.isEmpty() && !manualOverride && !antiCrawlOverride)
            {
                if (antiCrawl.getValue() && mc.player.getPose().equals(EntityPose.SWIMMING) && antiCrawlPos != null)
                {
                    MineData data = new MineData(antiCrawlPos, strictDirection.getValue() ?
                            MoneyStar.interactionManager.getInteractDirection(antiCrawlPos, false) : Direction.UP, MiningGoal.PREVENT_CRAWL);
                    if (isInstantMineComplete() || !instantMine.equals(data))
                    {
                        startAutoMine(data);
                        antiCrawlOverride = true;
                    }
                }

                else if (playerTarget != null && !targetChanged)
                {
                    BlockPos targetPos = EntityUtil.getRoundedBlockPos(playerTarget);
                    boolean bedrockPhased = PositionUtil.isBedrock(playerTarget.getBoundingBox(), targetPos) && !playerTarget.isCrawling();

                    if (!isInstantMineComplete() && checkDataY(instantMine, targetPos, bedrockPhased))
                    {
                        abortMining(instantMine);

                        instantMine = null;
                    }

                    else if (packetMine != null && checkDataY(packetMine, targetPos, bedrockPhased))
                    {

                        if (packetSwapBack)
                        {
                            MoneyStar.inventoryManager.syncToClient();
                            packetSwapBack = false;
                        }
                        packetMine = null;
                        waitForPacketMine = false;
                    }

                    else
                    {
                        List<BlockPos> phasedBlocks = getPhaseBlocks(playerTarget, targetPos, bedrockPhased);

                        MineData bestMine;
                        if (!phasedBlocks.isEmpty())
                        {
                            BlockPos pos1 = phasedBlocks.removeFirst();
                            bestMine = new MineData(pos1, strictDirection.getValue() ?
                                    MoneyStar.interactionManager.getInteractDirection(pos1, false) : Direction.UP);

                            if (packetMine == null && doubleBreak.getValue() || isInstantMineComplete())
                            {
                                startAutoMine(bestMine);
                            }
                        }

                        else
                        {
                            List<BlockPos> miningBlocks = getMiningBlocks(playerTarget, targetPos, bedrockPhased);
                            bestMine = getInstantMine(miningBlocks, bedrockPhased);

                            if (bestMine != null && (packetMine == null && !changedInstantMine
                                    && doubleBreak.getValue() || isInstantMineComplete()))
                            {
                                startAutoMine(bestMine);
                            }
                        }
                    }
                }

                else
                {
                    if (!isInstantMineComplete() && instantMine.getGoal() == MiningGoal.MINING_ENEMY)
                    {
                        abortMining(instantMine);
                        instantMine = null;
                    }

                    if (packetMine != null && packetMine.getGoal() == MiningGoal.MINING_ENEMY)
                    {
                        if (packetSwapBack)
                        {
                            MoneyStar.inventoryManager.syncToClient();
                            packetSwapBack = false;
                        }
                        packetMine = null;
                        waitForPacketMine = false;
                    }
                }
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
        if (event.getState().getBlock().getHardness() == -1.0f || !canMine(event.getState()) || isMining(event.getPos()))
        {
            return;
        }

        MineData data = new MineData(event.getPos(), event.getDirection(), MiningGoal.MANUAL);

        if (instantMine != null && instantMine.getGoal() == MiningGoal.MINING_ENEMY
                || packetMine != null && packetMine.getGoal() == MiningGoal.MINING_ENEMY)
        {
            manualOverride = true;
        }

        if (!doubleBreak.getValue())
        {
            instantMine = data;
            startMining(instantMine);
            mc.player.swingHand(Hand.MAIN_HAND, false);
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
        startMining(instantMine);
        mc.player.swingHand(Hand.MAIN_HAND, false);
        if (updateChanged)
        {
            changedInstantMine = true;
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchReset.getValue() && instantMine != null)
        {
            instantMine.setTotalBlockDamage(0.0f, 0.0f);
        }
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && canMine(packet.getState()))
        {
            if (antiCrawlOverride && packet.getPos().equals(getAntiCrawlPos(playerTarget)))
            {
                antiCrawlTicks = 10;
            }
        }
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

    public MineData getInstantMine(List<BlockPos> miningBlocks, boolean bedrockPhased)
    {
        PriorityQueue<MineData> validInstantMines = new PriorityQueue<>();
        for (BlockPos blockPos : miningBlocks)
        {
            BlockState state1 = mc.world.getBlockState(blockPos);
            if (!isAutoMineBlock(state1.getBlock())) // bedrock mine exploit!!
            {
                continue;
            }

            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > range.getValue())
            {
                continue;
            }

            BlockState state2 = mc.world.getBlockState(blockPos.down());
            if (bedrockPhased || state2.isOf(Blocks.OBSIDIAN) || state2.isOf(Blocks.BEDROCK))
            {
                Direction direction = strictDirection.getValue() ?
                        MoneyStar.interactionManager.getInteractDirection(blockPos, false) : Direction.UP;

                validInstantMines.add(new MineData(blockPos, direction));
            }
        }

        if (validInstantMines.isEmpty())
        {
            return null;
        }

        return validInstantMines.peek();
    }

    public List<BlockPos> getPhaseBlocks(PlayerEntity player, BlockPos playerPos, boolean targetBedrockPhased)
    {
        List<BlockPos> phaseBlocks = PositionUtil.getAllInBox(player.getBoundingBox(),
                targetBedrockPhased && head.getValue() ? playerPos.up() : playerPos);

        phaseBlocks.removeIf(p ->
        {
            BlockState state = mc.world.getBlockState(p);
            if (!isAutoMineBlock(state.getBlock()) || !canMine(state) || isMining(p))
            {
                return true;
            }

            double dist = mc.player.getEyePos().squaredDistanceTo(p.toCenterPos());
            if (dist > range.getValue())
            {
                return true;
            }

            return avoidSelf.getValue() && intersectsPlayer(p);
        });

        if (targetBedrockPhased && aboveHead.getValue())
        {
            phaseBlocks.add(playerPos.up(2));
        }

        return phaseBlocks;
    }

    /**
     *
     * @param player
     * @return A {@link Set} of potential blocks to mine for an enemy player
     */
    public List<BlockPos> getMiningBlocks(PlayerEntity player, BlockPos playerPos, boolean bedrockPhased)
    {
        List<BlockPos> surroundingBlocks = AutoFeetPlace.getInstance().getSurroundNoDown(player, range.getValue());
        List<BlockPos> miningBlocks;
        if (bedrockPhased)
        {
            List<BlockPos> facePlaceBlocks = new ArrayList<>();
            if (head.getValue())
            {
                facePlaceBlocks.addAll(surroundingBlocks.stream().map(BlockPos::up).toList());
            }

            BlockState belowFeet = mc.world.getBlockState(playerPos.down());
            if (canMine(belowFeet))
            {
                facePlaceBlocks.add(playerPos.down());
            }
            miningBlocks = facePlaceBlocks;
        }
        else
        {
            miningBlocks = surroundingBlocks;
        }

        miningBlocks.removeIf(p -> avoidSelf.getValue() && intersectsPlayer(p));
        return miningBlocks;
    }

    private BlockPos getAntiCrawlPos(PlayerEntity playerTarget)
    {
        if (!mc.player.isOnGround())
        {
            return null;
        }
        BlockPos crawlingPos = EntityUtil.getRoundedBlockPos(mc.player);
        boolean playerBelow = playerTarget != null && EntityUtil.getRoundedBlockPos(playerTarget).getY() < crawlingPos.getY();
        // We want to be same level as our opponent
        if (playerBelow)
        {
            BlockState state = mc.world.getBlockState(crawlingPos.down());
            if (isAutoMineBlock(state.getBlock()) && canMine(state))
            {
                return crawlingPos.down();
            }
        }
        else
        {
            BlockState state = mc.world.getBlockState(crawlingPos.up());
            if (isAutoMineBlock(state.getBlock()) && canMine(state))
            {
                return crawlingPos.up();
            }
        }
        return null;
    }

    private boolean checkDataY(MineData data, BlockPos targetPos, boolean bedrockPhased)
    {
        return data.getGoal() == MiningGoal.MINING_ENEMY && !bedrockPhased && data.getPos().getY() != targetPos.getY();
    }

    private boolean intersectsPlayer(BlockPos pos)
    {
        List<BlockPos> playerBlocks = AutoFeetPlace.getInstance().getPlayerBlocks(mc.player);
        List<BlockPos> surroundingBlocks = AutoFeetPlace.getInstance().getSurroundNoDown(mc.player);
        return playerBlocks.contains(pos) || surroundingBlocks.contains(pos);
    }





    public void startMining(MineData data)
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
        }

        if (rotate.getValue() && grim.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
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
        if (slot != -1)
        {
            swapTo(slot);
        }

        stopMiningInternal(data);

        if (slot != -1)
        {
            swapSync(slot);
        }

        if (rotate.getValue() && grim.getValue())
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

    public BlockPos getMiningBlock()
    {
        if (instantMine != null)
        {
            double damage = instantMine.getBlockDamage() / speed.getValue();
            if (damage > 0.75)
            {
                return instantMine.getPos();
            }
        }
        return null;
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

    public boolean isSilentSwapping()
    {
        return packetSwapBack;
    }

    private boolean isMining(BlockPos blockPos)
    {
        return instantMine != null && instantMine.getPos().equals(blockPos) ||
                packetMine != null && packetMine.getPos().equals(blockPos);
    }

    private boolean isAutoMineBlock(Block block)
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

    public static class MineData implements Comparable<MineData>
    {
        private final BlockPos pos;
        private final Direction direction;
        private final MiningGoal goal;
        //
        private int ticksMining;
        private float blockDamage, lastDamage;

        public MineData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
            this.goal = MiningGoal.MINING_ENEMY;
        }

        public MineData(BlockPos pos, Direction direction, MiningGoal goal)
        {
            this.pos = pos;
            this.direction = direction;
            this.goal = goal;
        }

        private double getPriority()
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(pos.down().toCenterPos());
            if (dist <= AutoCrystal.getInstance().getPlaceRange())
            {
                return 10.0f;
            }

            return 0.0f;
        }

        @Override
        public int compareTo(@NotNull MineData o)
        {
            return Double.compare(getPriority(), o.getPriority());
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

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public MiningGoal getGoal()
        {
            return goal;
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

        public static MineData empty()
        {
            return new MineData(BlockPos.ORIGIN, Direction.UP);
        }

        public MineData copy()
        {
            final MineData data = new MineData(pos, direction, goal);
            data.setTotalBlockDamage(blockDamage, lastDamage);
            return data;
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


    public enum MiningGoal
    {
        MANUAL,
        MINING_ENEMY,
        PREVENT_CRAWL
    }

    public enum RemineMode
    {
        INSTANT,
        NORMAL,
        FAST
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }
}


