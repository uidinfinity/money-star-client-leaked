package me.money.star.client.modules.combat;

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
import java.util.List;
import java.util.*;


public class AutoFeetPlace extends ObsidianPlacerModule
{
    private static AutoFeetPlace INSTANCE;
    public Setting<Timing> timing = mode("Timing", Timing.VANILLA);
    public Setting<Boolean> prePlaceExplosion = bool("Pre-Place-Explosions", false);
    public Setting<Boolean> prePlaceTick = bool("Pre-Place-Tick", true);;
    public Setting<Float> placeRange = num("Place-Range", 4.0f, 0f, 6.0f);
    public Setting<Boolean> attack = bool("Attack", true);
    public Setting<Boolean> extend = bool("Extend", false);
    public Setting<Boolean> head = bool("Cover-Head", false);
    public Setting<Boolean> mineExtend = bool("Mine-Extend", false);
    public Setting<Boolean> support = bool("Support-Blocks", true);;
    public Setting<Integer> shiftTicks = num("Shift-Ticks", 1, 2, 10);
    public Setting<Float> shiftDelay = num("Shift-Delay", 1.0f, 0f, 5.0f);
    public Setting<Boolean> jumpDisable = bool("JumpDisable", true);
    public Setting<Boolean> disableDeath = bool("DisableOnDeath", true);


    private int blocksPlaced;
    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();

    private double prevY;

    public AutoFeetPlace()
    {
        super("AutoFeetPlace", "Surrounds feet with obsidian", Category.COMBAT,true,false,false, 950);
        INSTANCE = this;
    }

    public static AutoFeetPlace getInstance()
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
       // if (SelfTrapModule.getInstance().isEnabled()) {return;}

        if (jumpDisable.getValue() && (mc.player.getY() - prevY > 0.5 || mc.player.fallDistance > 1.5f))
        {
            disable();
            return;
        }

        if (!multitask.getValue() && checkMultitask())
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

        surround = getSurround(mc.player);
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
            return;
        }

        if (support.getValue())
        {
            for (BlockPos block : new ArrayList<>(placements))
            {
                if (block.getY() > mc.player.getBlockY() + 1.0)
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
        if (timing.getValue() != Timing.SEQUENTIAL)
        {
            return;
        }

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

        if (blocksPlaced > shiftTicks.getValue() * 2) // Give some leniency if we are getting place on
        {
            return;
        }

        if (serverPacket instanceof ExplosionS2CPacket packet && prePlaceExplosion.getValue())
        {
            BlockPos pos = BlockPos.ofFloored(packet.center().getX(), packet.center().getY(), packet.center().getZ());
            if (surround.contains(pos))
            {
                final int slot = getResistantBlockItem();
                if (slot == -1)
                {
                    return;
                }
                placeBlock(pos, slot);
            }
        }

        if (serverPacket instanceof EntitySpawnS2CPacket packet
                && packet.getEntityType().equals(EntityType.END_CRYSTAL) && prePlaceTick.getValue())
        {
            for (BlockPos pos : surround)
            {
                if (!pos.equals(BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ())))
                {
                    continue;
                }

                final int slot = getResistantBlockItem();
                if (slot == -1)
                {
                    return;
                }
                placeBlock(pos, slot);
                break;
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
        blocksPlaced++;
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
            if (dist > placeRange.getValue()){
                continue;
            }

            if (mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, surroundPos, ShapeContext.absent()))
            {
                placements.add(surroundPos);
            }
        }
        return placements;
    }

    public List<BlockPos> getSurround(PlayerEntity player)
    {
        List<BlockPos> surroundBlocks = getSurroundNoDown(player);
        List<BlockPos> playerBlocks = getPlayerBlocks(player);
        for (BlockPos playerPos : playerBlocks)
        {
            if (playerPos.equals(player.getBlockPos()))
            {
                continue;
            }
            surroundBlocks.add(playerPos.down());
        }
        if (mineExtend.getValue())
        {
            for (BlockPos surroundPos : new ArrayList<>(surroundBlocks))
            {
                if (!MoneyStar.blockManager.isPassed(surroundPos, 0.7f))
                {
                    continue;
                }
                for (Direction direction : Direction.values())
                {
                    if (direction == Direction.DOWN)
                    {
                        continue;
                    }
                    BlockPos blockerPos = surroundPos.offset(direction);

                    if (playerBlocks.contains(blockerPos)
                            || AutoMine.getInstance().getMiningBlock() == blockerPos) // Dont want to help our opponent surround
                    {
                        continue;
                    }


                    surroundBlocks.add(blockerPos);
                }
            }
        }

        if (MoneyStar.moduleManager.getModuleByClass(AirPlace.class).isEnabled() && head.getValue())
        {
            surroundBlocks.add(mc.player.getBlockPos().up(2));
        }
        return surroundBlocks;
    }

    public List<BlockPos> getSurroundNoDown(PlayerEntity player)
    {
        return getSurroundNoDown(player, 0.0f);
    }

    public List<BlockPos> getSurroundNoDown(PlayerEntity player, float range)
    {
        List<BlockPos> surroundBlocks = new ArrayList<>();
        List<BlockPos> playerBlocks = getPlayerBlocks(player);
        for (BlockPos pos : playerBlocks)
        {
            if (range > 0.0f && mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos()) > range * range)
            {
                continue;
            }
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
            }
        }
        return surroundBlocks;
    }

    public List<BlockPos> getPlayerBlocks(PlayerEntity entity)
    {
        BlockPos playerPos = PositionUtil.getRoundedBlockPos(entity.getX(), entity.getY(), entity.getZ());
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

    public enum Timing
    {
        VANILLA,
        SEQUENTIAL
    }
}
