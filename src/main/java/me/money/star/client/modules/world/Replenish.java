package me.money.star.client.modules.world;
import me.money.star.event.impl.entity.EntityDeathEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.InventoryUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import net.minecraft.util.Hand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class Replenish extends Module {
    private static Replenish INSTANCE;


    public Setting<Integer> percent = num("Percent", 30, 1, 80);
    public Setting<Boolean> resistant = bool("Allow-Resistant", false);

    private final Map<Integer, ItemStack> hotbarCache = new ConcurrentHashMap<>();

    private final Timer lastDroppedTimer = new CacheTimer();


    public Replenish() {
        super("Replenish", "Automatically replaces items in your hotbar", Category.WORLD,true,false,false);
        INSTANCE = this;

    }

    public static Replenish getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        hotbarCache.clear();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        hotbarCache.clear();
    }

    @Subscribe
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof ClientPlayerEntity)
        {
            hotbarCache.clear();
        }
    }

    @Subscribe
    public void onTick(PlayerTickEvent event)
    {
        if (mc.options.dropKey.isPressed())
        {
            lastDroppedTimer.reset();
        }

        boolean pauseReplenish = isInInventoryScreen() || !lastDroppedTimer.passed(100);

        if (!pauseReplenish)
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty())
                {
                    ItemStack cachedStack = hotbarCache.getOrDefault(i, null);
                    if (cachedStack != null && !cachedStack.isEmpty())
                    {
                        replenishStack(i, cachedStack);
                        break;
                    }
                    continue;
                }

                if (!stack.isStackable())
                {
                    continue;
                }

                double percentage = ((double) stack.getCount() / stack.getMaxCount()) * 100.0;
                if (percentage <= percent.getValue())
                {
                    replenishStack(i, stack);
                    break;
                }
            }
        }

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() && !pauseReplenish)
            {
                continue;
            }

            if (hotbarCache.containsKey(i))
            {
                hotbarCache.replace(i, stack.copy());
            }
            else
            {
                hotbarCache.put(i, stack.copy());
            }
        }
    }

    public boolean isInInventoryScreen()
    {
        return mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof ShulkerBoxScreen || mc.currentScreen instanceof InventoryScreen;
    }

    private void replenishStack(int slot, ItemStack stack)
    {
        int slot1 = -1;
        boolean outOfObsidian = stack.getItem() == Items.OBSIDIAN && InventoryUtil.count(Items.OBSIDIAN) <= 1;
        for (int i = 9; i < 36; ++i)
        {


            ItemStack itemStack = mc.player.getInventory().getStack(i);

            if (itemStack.isEmpty())
            {
                continue;
            }

            if (!isSame(stack, itemStack, outOfObsidian) || !itemStack.isStackable())
            {
                continue;
            }

            slot1 = i;
        }

        if (slot1 != -1)
        {
            // sendModuleError("slot: " + slot + ", stack:" + stack.getName().getString());
            mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, slot + 36, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            {
                mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            }
        }
    }

    public boolean isSame(ItemStack stack1, ItemStack stack2, boolean outOfObsidian)
    {
        if (resistant.getValue() && stack1.getItem() == Items.OBSIDIAN && outOfObsidian)
        {
            return stack2.getItem() == Items.ENDER_CHEST || stack2.getItem() == Items.CRYING_OBSIDIAN;
        }

        else if (stack1.getItem() instanceof BlockItem blockItem
                && (!(stack2.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock()))
        {
            return false;
        }

        else if (!stack1.getName().getString().equals(stack2.getName().getString()))
        {
            return false;
        }

        return stack1.getItem().equals(stack2.getItem());
    }
}