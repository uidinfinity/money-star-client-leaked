package me.money.star.util.player;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.money.star.MoneyStar;
import me.money.star.client.manager.player.InventoryManager;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.util.traits.Util;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;

/**
 * @author linus
 * @since 1.0
 */
public class InventoryUtil implements Util {
    public static boolean isHolding32k()
    {
        return isHolding32k(1000);
    }

    /**
     * @param lvl
     * @return
     */
    public static boolean isHolding32k(int lvl)
    {
        final ItemStack mainhand = mc.player.getMainHandStack();
        return EnchantmentUtil.getLevel(mainhand, Enchantments.SHARPNESS) >= lvl;
    }

    public static boolean hasItemInInventory(final Item item, final boolean hotbar)
    {
        final int startSlot = hotbar ? 0 : 9;
        for (int i = startSlot; i < 36; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() == item)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean hasItemInHotbar(final Item item)
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() == item)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param item
     * @return
     */
    public static int count(Item item)
    {
        if (mc.player == null)
        {
            return 0;
        }
        ItemStack offhandStack = mc.player.getOffHandStack();
        int itemCount = offhandStack.getItem() == item ? offhandStack.getCount() : 0;
        for (int i = 0; i < 36; i++)
        {
            ItemStack slot = mc.player.getInventory().getStack(i);
            if (slot.getItem() == item)
            {
                itemCount += slot.getCount();
            }
        }
        return itemCount;
    }
}
