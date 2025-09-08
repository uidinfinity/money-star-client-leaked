package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.BlockPlacerModule;
import me.money.star.client.gui.modules.ObsidianPlacerModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;



public class AutoWeb extends BlockPlacerModule
{
    private static AutoWeb INSTANCE;


    public Setting<Float> range = num("Place-Range", 4.0f, 0f, 6.0f);
    public Setting<Float> enemyRange = num("EnemyRange", 10.0f, 0.1f, 15.0f);
    public Setting<Boolean> head = bool("Cover-Head", false);
    public Setting<Integer> shiftTicks = num("Shift-Ticks", 1, 2, 10);
    public Setting<Float> shiftDelayButton = num("Shift-Delay", 1.0f, 0f, 5.0f);
    public Setting<Boolean> disableDeath = bool("DisableOnDeath", true);


    private int shiftDelay;
    private List<BlockPos> webs = new ArrayList<>();



    public AutoWeb()
    {
        super("AutoWeb", "Surrounds feet with obsidian", Category.COMBAT,true,false,false, 950);
        INSTANCE = this;
    }

    public static AutoWeb getInstance()
    {
        return INSTANCE;
    }



    @Override
    public void onDisable()
    {

        webs.clear();
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
        if (!multitask.getValue() && checkMultitask())
        {
            webs.clear();
            return;
        }

        int blocksPlaced = 0;
        int slot = getBlockItemSlot(Blocks.COBWEB);
        if (slot == -1)
        {
            webs.clear();
            return;
        }

        if (shiftDelay < shiftDelayButton.getValue())
        {
            shiftDelay++;
            return;
        }
        List<BlockPos> webPlacements = new ArrayList<>();
        for (PlayerEntity entity : mc.world.getPlayers())
        {
            if (entity == mc.player || MoneyStar.friendManager.isFriend(entity.getName().getString()))
            {
                continue;
            }
            double d = mc.player.distanceTo(entity);
            if (d > enemyRange.getValue())
            {
                continue;
            }
            BlockPos feetPos = entity.getBlockPos();
            double dist = mc.player.getEyePos().squaredDistanceTo(feetPos.toCenterPos());
            if (mc.world.getBlockState(feetPos).isAir() && dist <=  range.getValue())
            {
                webPlacements.add(feetPos);
            }
            if (head.getValue())
            {
                BlockPos headPos = feetPos.up();
                double dist2 = mc.player.getEyePos().squaredDistanceTo(headPos.toCenterPos());
                if (mc.world.getBlockState(headPos).isAir() && dist2 <=  range.getValue())
                {
                    webPlacements.add(headPos);
                }
            }
        }
        webs = webPlacements;
        if (webs.isEmpty())
        {
            return;
        }
        while (blocksPlaced < shiftTicks.getValue())
        {
            if (blocksPlaced >= webs.size())
            {
                break;
            }
            BlockPos targetPos = webs.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeWeb(targetPos, slot);
        }

        if (rotate.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
        }
    }

   
    private void placeWeb(BlockPos pos, int slot)
    {
        MoneyStar.interactionManager.placeBlock(pos, slot, strictDirection.getValue(), false, (state, angles) ->
        {
            if (rotate.getValue() && state)
            {
                MoneyStar.rotationManager.setRotationSilent(angles[0], angles[1]);
            }
        });
    }

    public boolean isPlacing()
    {
        return !webs.isEmpty();
    }
}