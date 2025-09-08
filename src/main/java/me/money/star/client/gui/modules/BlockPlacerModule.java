package me.money.star.client.gui.modules;

import me.money.star.client.settings.Setting;
import java.util.function.Predicate;

import me.money.star.util.traits.Util;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
public class BlockPlacerModule extends CombatModule {
    protected Setting<Boolean> strictDirection = bool("Strict-Direction", false);
    protected Setting<Boolean> rotate = bool("Rotate", false);


    public BlockPlacerModule(String name, String description, Module.Category category, boolean hasListener, boolean hidden, boolean alwaysListening)
    {
        super(name, description, category,hasListener,hidden,alwaysListening);
    }

    public BlockPlacerModule(String name, String description, Module.Category category, boolean hasListener, boolean hidden, boolean alwaysListening, int rotationPriority)
    {
        super(name, description, category,hasListener,hidden,alwaysListening, rotationPriority);

    }

    protected int getSlot(final Predicate<ItemStack> filter)
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = Util.mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && filter.test(itemStack))
            {
                return i;
            }
        }
        return -1;
    }

    protected int getBlockItemSlot(final Block block)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Util.mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() == block)
            {
                return i;
            }
        }
        return -1;
    }
}
