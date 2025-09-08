package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.combat.AutoCrystal;
import me.money.star.client.modules.combat.AutoFeetPlace;
import me.money.star.event.impl.network.AttackBlockEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;

public final class AutoTool extends Module
{
    private static AutoTool INSTANCE;
    public AutoTool()
    {
        super("AutoTool", "Automatically switches to a tool before mining", Category.WORLD,true,false,false);
        INSTANCE = this;
    }
    public static AutoTool getInstance()
    {
        return INSTANCE;
    }

    @Subscribe
    public void onBreakBlock(final AttackBlockEvent event)
    {
        final BlockState state = Util.mc.world.getBlockState(event.getPos());
        final int blockSlot = getBestToolNoFallback(state);
        if (blockSlot != -1)
        {
            Util.mc.player.getInventory().selectedSlot = blockSlot;
        }
    }

    public int getBestTool(final BlockState state) {
        int slot = getBestToolNoFallback(state);
        if (slot != -1) {
            return slot;
        }
        return Util.mc.player.getInventory().selectedSlot;
    }

    public int getBestToolNoFallback(final BlockState state)
    {
        int slot = -1;
        float bestTool = 0.0f;
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Util.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof MiningToolItem))
            {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            //final int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            //if (efficiency > 0)
            //{
            //    speed += efficiency * efficiency + 1.0f;
           // }
            if (speed > bestTool)
            {
                bestTool = speed;
                slot = i;
            }
        }
        return slot;
    }
}
