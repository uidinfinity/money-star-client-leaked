package me.money.star.client.modules.combat;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.ObsidianPlacerModule;
import me.money.star.client.modules.world.AirPlace;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.math.position.PositionUtil;
import me.money.star.util.world.BlastResistantBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.*;


public class AutoTrap extends ObsidianPlacerModule
{
    private static AutoTrap INSTANCE;


    public Setting<Float> placeRange = num("Place-Range", 4.0f, 0f, 6.0f);
    public Setting<Boolean> attack = bool("Attack", true);
    public Setting<Boolean> antiStep = bool("AntiStep", false);
    public Setting<Boolean> extend = bool("Extend", false);
    public Setting<Boolean> head = bool("Cover-Head", false);
    public Setting<Boolean> support = bool("Support-Blocks", true);;
    public Setting<Integer> shiftTicks = num("Shift-Ticks", 1, 2, 10);
    public Setting<Float> shiftDelay = num("Shift-Delay", 1.0f, 0f, 5.0f);
    public Setting<Boolean> autoDisable = bool("AutoDisable", true);
    public Setting<Boolean> disableDeath = bool("DisableOnDeath", true);


    private int blocksPlaced;
    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();

    private double prevY;

    public AutoTrap()
    {
        super("AutoTrap", "Surrounds feet with obsidian", Category.COMBAT,true,false,false, 950);
        INSTANCE = this;
    }

    public static AutoTrap getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null)
        {
            return;
        }
        prevY = mc.player.getY();
    }

    @Override
    public void onDisable()
    {
        surround.clear();
        placements.clear();
        packets.clear();

    }
    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        if (disableDeath.getValue())
        {
            disable();
        }
    }

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {
        blocksPlaced = 0;

        if (!multitask.getValue() && mc.player.isUsingItem())
        {
            surround.clear();
            placements.clear();
            return;
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            surround.clear();
            placements.clear();
            return;
        }
        PlayerEntity trapTarget = getTrapTarget();
        if (trapTarget == null)
        {
            surround.clear();
            placements.clear();
            return;
        }

        BlockPos targetBlockPos = PositionUtil.getRoundedBlockPos(trapTarget.getX(), trapTarget.getY(), trapTarget.getZ());
        surround = getSurround(targetBlockPos, trapTarget);
        if (surround.isEmpty())
        {
            return;
        }
        if (attack.getValue())
        {
            attackBlockingCrystals(surround);
        }
        placements = getPlacementsFromSurround(surround);
        if (placements.isEmpty())
        {
            if (autoDisable.getValue())
            {
                disable();
            }
            return;
        }
        if (support.getValue())
        {
            for (BlockPos block : new ArrayList<>(placements))
            {
                if (block.getY() > targetBlockPos.getY())
                {
                    continue;
                }
                Direction direction = MoneyStar.interactionManager.getInteractDirectionInternal(block, strictDirection.getValue());
                if (direction == null)
                {
                    placements.add(block.down());
                }
            }
        }
        placements.sort(Comparator.comparingInt(Vec3i::getY));
        while (blocksPlaced < shiftTicks.getValue())
        {
            if (blocksPlaced >= placements.size())
            {
                break;
            }
            BlockPos targetPos = placements.get(blocksPlaced);
            blocksPlaced++;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPos, slot);
        }

        if (rotate.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
        }
    }

    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handlePackets(packet1);
            }
        }
        else
        {
            handlePackets(event.getPacket());
        }
    }

    private void handlePackets(Packet<?> serverPacket)
    {
        if (serverPacket instanceof BlockUpdateS2CPacket packet)
        {
            final BlockState blockState = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos))
            {
                if (blockState.isReplaceable() && mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, targetPos, ShapeContext.absent()))
                {
                    final int slot = getResistantBlockItem();
                    if (slot == -1)
                    {
                        return;
                    }
                    placeBlock(targetPos, slot);
                }
                else if (BlastResistantBlocks.isBlastResistant(blockState))
                {
                    packets.remove(targetPos);
                }
            }
        }
    }

    private void placeBlock(BlockPos pos, int slot)
    {
        MoneyStar.interactionManager.placeBlock(pos, slot, strictDirection.getValue(), false, true, (state, angles) ->
        {
            if (rotate.getValue() && state)
            {
                MoneyStar.rotationManager.setRotationSilent(angles[0], angles[1]);
            }
        });
        packets.put(pos, System.currentTimeMillis());
    }

    private PlayerEntity getTrapTarget()
    {
        final List<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        return (PlayerEntity) entities.stream()
                .filter(e -> e instanceof PlayerEntity && e.isAlive() && mc.player != e && !MoneyStar.friendManager.isFriend(e.getName().getString()))
                .filter(e -> mc.player.squaredDistanceTo(e) <= placeRange.getValue())
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)))
                .orElse(null);
    }

    public void attackBlockingCrystals(List<BlockPos> posList)
    {
        for (BlockPos pos : posList)
        {
            Entity crystalEntity = mc.world.getOtherEntities(null, new Box(pos)).stream()
                    .filter(e -> e instanceof EndCrystalEntity).findFirst().orElse(null);
            if (crystalEntity == null)
            {
                continue;
            }
            MoneyStar.networkManager.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            return;
        }
    }

    public List<BlockPos> getPlacementsFromSurround(List<BlockPos> surround)
    {
        List<BlockPos> placements = new ArrayList<>();
        for (BlockPos surroundPos : surround)
        {
            Long placed = packets.get(surroundPos);
            if (shiftDelay.getValue() > 0.0f && placed != null && System.currentTimeMillis() - placed < shiftDelay.getValue() * 50.0f)
            {
                continue;
            }
            if (!mc.world.getBlockState(surroundPos).isReplaceable())
            {
                continue;
            }
            double dist = mc.player.squaredDistanceTo(surroundPos.toCenterPos());
            if (dist > placeRange.getValue())
            {
                continue;
            }

            if (mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, surroundPos, ShapeContext.absent()))
            {
                placements.add(surroundPos);
            }
        }
        return placements;
    }

    public List<BlockPos> getSurround(BlockPos playerPos, PlayerEntity player)
    {
        List<BlockPos> surroundBlocks = new ArrayList<>();
        List<BlockPos> playerBlocks = getPlayerBlocks(playerPos, player);
        for (BlockPos pos : playerBlocks)
        {
            for (Direction dir : Direction.values())
            {
                if (!dir.getAxis().isHorizontal())
                {
                    continue;
                }
                BlockPos pos1 = pos.offset(dir);
                if (surroundBlocks.contains(pos1) || playerBlocks.contains(pos1))
                {
                    continue;
                }

                surroundBlocks.add(pos1);
                surroundBlocks.add(pos1.up());
            }
        }
        if (head.getValue())
        {
            boolean support = false;
            final List<BlockPos> headBlocks = new ArrayList<>();
            for (BlockPos pos : playerBlocks)
            {
                BlockPos headPos = pos.offset(Direction.UP, 2);
                if (!mc.world.getBlockState(headPos).isReplaceable())
                {
                    support = true;
                }
                headBlocks.add(headPos);
                if (antiStep.getValue())
                {
                    BlockPos antiStepPos = pos.offset(Direction.UP, 3);
                    headBlocks.add(antiStepPos);
                }
            }
            if (!MoneyStar.moduleManager.getModuleByClass(AirPlace.class).isEnabled())
            {
                BlockPos supportingPos = null;
                double min = Double.MAX_VALUE;
                for (BlockPos pos : surroundBlocks)
                {
                    BlockPos pos1 = pos.offset(Direction.UP, 2);
                    if (!mc.world.getBlockState(pos1).isReplaceable())
                    {
                        support = true;
                        break;
                    }
                    double dist = mc.player.squaredDistanceTo(pos1.toCenterPos());
                    if (dist < min)
                    {
                        supportingPos = pos1;
                        min = dist;
                    }
                }
                if (supportingPos != null && !support)
                {
                    surroundBlocks.add(supportingPos);
                }
            }
            surroundBlocks.addAll(headBlocks);
        }
        return surroundBlocks;
    }

    public List<BlockPos> getPlayerBlocks(BlockPos playerPos, PlayerEntity entity)
    {
        final List<BlockPos> playerBlocks = new ArrayList<>();
        if (extend.getValue())
        {
            playerBlocks.addAll(PositionUtil.getAllInBox(entity.getBoundingBox(), playerPos));
        }
        else
        {
            playerBlocks.add(playerPos);
        }
        return playerBlocks;
    }



    public boolean isPlacing()
    {
        return !placements.isEmpty();
    }
}