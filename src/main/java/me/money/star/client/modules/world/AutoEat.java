package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.MoneyStarGui;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.Modules;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.TickEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoEat extends Module
{

    public Setting<Float> hunger = num("Hunger", 19.0f, 1.0f, 20.0f);



    public AutoEat()
    {
        super("AutoEat", "Automatically eats when losing hunger",
                Category.WORLD,true,false,false);
    }

    @Override
    public void onDisable()
    {
        mc.options.useKey.setPressed(false);
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        //
        HungerManager hungerManager = mc.player.getHungerManager();
        if (hungerManager.getFoodLevel() <= hunger.getValue())
        {
            int slot = getFoodSlot();
            if (slot == -1)
            {
                return;
            }
            if (slot == 45)
            {
                mc.player.setCurrentHand(Hand.OFF_HAND);
            }
            else
            {
                MoneyStar.inventoryManager.setClientSlot(slot);
            }
            mc.options.useKey.setPressed(true);
        }
        else
        {
            mc.options.useKey.setPressed(false);
        }
    }

    public int getFoodSlot()
    {
        int foodLevel = -1;
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem().getComponents().contains(DataComponentTypes.FOOD))
            {
                if (stack.getItem() == Items.PUFFERFISH
                        || stack.getItem() == Items.CHORUS_FRUIT)
                {
                    continue;
                }
                int hunger = stack.getItem().getComponents().get(DataComponentTypes.FOOD).nutrition();
                if (hunger > foodLevel)
                {
                    slot = i;
                    foodLevel = hunger;
                }
            }
        }
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem().getComponents().contains(DataComponentTypes.FOOD))
        {
            if (offhand.getItem() == Items.PUFFERFISH
                    || offhand.getItem() == Items.CHORUS_FRUIT)
            {
                return slot;
            }
            int hunger = offhand.getItem().getComponents().get(DataComponentTypes.FOOD).nutrition();
            if (hunger > foodLevel)
            {
                slot = 45;
            }
        }
        return slot;
    }
}
