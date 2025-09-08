package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.RotationModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.math.timer.TickTimer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoExp extends RotationModule
{
    private static AutoExp INSTANCE;
    public Setting<Boolean> multiTask = bool("MultiTask", true);
    public Setting<Float> delay = num("Delay", 1.0f, 1.0f, 10.0f);
    public Setting<Integer> shiftTicks = num("Shift-Ticks", 1, 1, 64);
    public Setting<Boolean> durabilityCheck = bool("Durability-Check", false);
    // public Setting<Boolean> rotate = bool("Rotate", true);
    public Setting<Boolean> swing = bool("Swing", false);

    private final TickTimer delayTimer = new TickTimer();

    public AutoExp()
    {
        super("AutoExp", "Automatically throws exp silently.", Category.COMBAT,true,false,false, 850);
        INSTANCE = this;
    }

    public static AutoExp getInstance()
    {
        return INSTANCE;
    }



    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {

        if (mc.player == null || !delayTimer.passed(delay.getValue()))
        {
            return;
        }

        if (mc.player.isUsingItem() && !multiTask.getValue())
        {
            return;
        }

        if (durabilityCheck.getValue() && areItemsFullDura(mc.player))
        {
            disable();
            return;
        }

        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ExperienceBottleItem)
            {
                slot = i;
                break;
            }
        }
        if (slot == -1)
        {
            disable();
            return;
        }

        MoneyStar.inventoryManager.setSlot(slot);
      //  if (rotate.getValue())
      //  {
        //    setRotation(mc.player.getYaw(),90);
        //    if (isRotationBlocked()) return;
      //  }
        for (int i = 0; i < shiftTicks.getValue(); i++)
        {
            MoneyStar.networkManager.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            if (swing.getValue())
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
        MoneyStar.inventoryManager.syncToClient();
        delayTimer.reset();
    }

    private boolean areItemsFullDura(PlayerEntity player)
    {
        if (!isItemFullDura(player.getMainHandStack()) || !isItemFullDura(player.getOffHandStack()))
        {
            return false;
        }

        for (ItemStack stack : player.getArmorItems())
        {
            if (!isItemFullDura(stack))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isItemFullDura(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return true;
        }
        int maxDura = stack.getMaxDamage();
        int currentDura = stack.getDamage();
        return currentDura == 0 || maxDura == 0;
    }
}
