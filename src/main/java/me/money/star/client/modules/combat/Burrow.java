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
import me.money.star.util.world.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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


public class Burrow extends ObsidianPlacerModule
{
    private static Burrow INSTANCE;
    public Setting<Mode> mode = mode("Mode", Mode.BLOCK_LAG);
    public Setting<Boolean> attack = bool("Attack", false);
    public Setting<Boolean> autoDisable = bool("AutoDisable", true);;
   

    private int blocksPlaced;
    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();

    private double prevY;

    public Burrow()
    {
        super("Burrow", "Surrounds feet with obsidian", Category.COMBAT,true,false,false, 950);
        INSTANCE = this;
    }

    public static Burrow getInstance()
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

    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        disable();
    }

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (Math.abs(mc.player.getY() - prevY) > 0.5)
        {
            disable();
            return;
        }

        if (checkMultitask() && !multitask.getValue())
        {
            return;
        }

        boolean inBlock = PositionUtil.getAllInBox(mc.player.getBoundingBox()).stream().anyMatch(p -> !mc.world.getBlockState(p).isReplaceable());
        final BlockPos pos = EntityUtil.getRoundedBlockPos(mc.player);
        if (!inBlock && mc.player.isOnGround())
        {
            if (mode.getValue() == Mode.BLOCK_LAG)
            {
                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 0.42,
                        mc.player.getZ(), true,mc.player.horizontalCollision));
                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 0.75,
                        mc.player.getZ(), true,mc.player.horizontalCollision));
                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 1.01,
                        mc.player.getZ(), true,mc.player.horizontalCollision));
                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 1.16,
                        mc.player.getZ(), true,mc.player.horizontalCollision));

                if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, 2.34, 0.0)))
                {
                    return;
                }

                double y = mc.player.getY();
                MoneyStar.positionManager.setPositionClient(mc.player.getX(), y + 1.167, mc.player.getZ());
                attackPlace(pos);
                MoneyStar.positionManager.setPositionClient(mc.player.getX(), y, mc.player.getZ());

                MoneyStar.networkManager.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.34, mc.player.getZ(), false,mc.player.horizontalCollision));
            }

            else if (mode.getValue() == Mode.WEB)
            {
                int slot = getBlockItemSlot(Blocks.COBWEB);
                if (slot == -1)
                {
                    return;
                }
                MoneyStar.interactionManager.placeBlock(pos, slot, strictDirection.getValue(), false, (state, angles) ->
                {
                    if (rotate.getValue())
                    {
                        if (state)
                        {
                            MoneyStar.rotationManager.setRotationSilent(angles[0], angles[1]);
                        }
                        else
                        {
                            MoneyStar.rotationManager.setRotationSilentSync();
                        }
                    }
                });
            }
        }

        if (autoDisable.getValue())
        {
            disable();
        }
    }

    private void attackPlace(BlockPos targetPos)
    {
        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        attackPlace(targetPos, slot);
    }

    private void attackPlace(BlockPos targetPos, int slot)
    {
        if (attack.getValue())
        {
            Entity entity = mc.world.getOtherEntities(null, new Box(targetPos)).stream().filter(e -> e instanceof EndCrystalEntity).findFirst().orElse(null);
            if (entity != null)
            {
                MoneyStar.networkManager.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }

        MoneyStar.interactionManager.placeBlock(targetPos, slot, false, strictDirection.getValue(), false, (state, angles) ->
        {
            if (rotate.getValue() && state)
            {
                MoneyStar.rotationManager.setRotationSilent(angles[0], angles[1]);
            }
        });
    }

    private enum Mode
    {
        BLOCK_LAG,
        WEB
    }
}