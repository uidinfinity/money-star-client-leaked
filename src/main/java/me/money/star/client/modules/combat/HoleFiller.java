package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.ObsidianPlacerModule;
import me.money.star.client.manager.combat.hole.Hole;
import me.money.star.client.manager.combat.hole.HoleType;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;


public class HoleFiller extends ObsidianPlacerModule
{
    private static HoleFiller INSTANCE;
    public Setting<Boolean> webs = bool("Webs", false);
    public Setting<Boolean> obsidian = bool("Obsidian", false);
    public Setting<Boolean> doubles = bool("Doubles", true);;
    public Setting<Boolean> auto = bool("Auto", true);;
    public Setting<Float> targetRange = num("TargetRange", 3.0f, 0.5f, 5.0f);
    public Setting<Float> enemyRange = num("EnemyRange", 10.0f, 0.1f, 15.0f);
    public Setting<Float> range = num("PlaceRange", 4.0f, 0f, 6.0f);
    public Setting<Boolean> attack = bool("Attack", true);
    public Setting<Integer> shiftTicks = num("ShiftTicks", 1, 2, 10);
    public Setting<Float> shiftDelayButton = num("ShiftDelay", 1.0f, 0f, 5.0f);
    public Setting<Boolean> autoDisable = bool("AutoDisable", true);
    public Setting<Boolean> disableDeath = bool("DisableOnDeath", true);


    private int shiftDelay;

    private List<BlockPos> fills = new ArrayList<>();
    public HoleFiller()
    {
        super("HoleFiller", "Surrounds feet with obsidian", Category.COMBAT,true,false,false, 950);
        INSTANCE = this;
    }

    public static HoleFiller getInstance()
    {
        return INSTANCE;
    }



    @Override
    public void onDisable()
    {
        fills.clear();
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
        //
        int blocksPlaced = 0;

        if (!multitask.getValue() && checkMultitask())
        {
            fills.clear();
            return;
        }

        final int slot = webs.getValue() ? getBlockItemSlot(Blocks.COBWEB) : getResistantBlockItem();
        if (slot == -1)
        {
            fills.clear();
            return;
        }

        if (shiftDelayButton.getValue() > 0 && shiftDelay < shiftDelayButton.getValue())
        {
            shiftDelay++;
            return;
        }
        List<BlockPos> holes = new ArrayList<>();
        for (Hole hole : MoneyStar.holeManager.getHoles())
        {
            if (hole.isQuad() || hole.isDouble() && !doubles.getValue() || hole.getSafety() == HoleType.OBSIDIAN && !obsidian.getValue())
            {
                continue;
            }
            if (hole.squaredDistanceTo(mc.player) > range.getValue())
            {
                continue;
            }

            if (!mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, hole.getPos(), ShapeContext.absent()))
            {
                continue;
            }

            if (auto.getValue())
            {
                for (PlayerEntity entity : mc.world.getPlayers())
                {
                    if (entity == mc.player || MoneyStar.friendManager.isFriend(entity.getName().getString()))
                    {
                        continue;
                    }
                    double dist = mc.player.distanceTo(entity);
                    if (dist > enemyRange.getValue())
                    {
                        continue;
                    }
                    if (entity.getY() >= hole.getY() &&
                            hole.squaredDistanceTo(entity) > targetRange.getValue())
                    {
                        continue;
                    }
                    holes.add(hole.getPos());
                    break;
                }
            }
            else
            {
                holes.add(hole.getPos());
            }
        }
        fills = holes;
        if (fills.isEmpty())
        {
            if (autoDisable.getValue())
            {
                disable();
            }
            return;
        }
        if (attack.getValue())
        {
            attackBlockingCrystals(fills);
        }
        while (blocksPlaced < shiftTicks.getValue())
        {
            if (blocksPlaced >= fills.size())
            {
                break;
            }
            BlockPos targetPos = fills.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPos, slot);
        }

        if (rotate.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
        }
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

    private void placeBlock(BlockPos targetPos, int slot)
    {
        MoneyStar.interactionManager.placeBlock(targetPos, slot, strictDirection.getValue(), false, (state, angles) ->
        {
            if (rotate.getValue() && state)
            {
                MoneyStar.rotationManager.setRotationSilent(angles[0], angles[1]);
            }
        });
    }


    public boolean isPlacing()
    {
        return !fills.isEmpty();
    }
}