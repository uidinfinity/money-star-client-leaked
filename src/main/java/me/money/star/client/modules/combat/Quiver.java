package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.RotationModule;
import me.money.star.event.impl.network.PlayerTickEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;


import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Quiver extends RotationModule
{
    private static Quiver INSTANCE;

    private int pullTicks;
    private final Set<StatusEffectInstance> arrows = new HashSet<>();

    public Quiver()
    {
        super("Quiver", "Shoots player with beneficial tipped arrows", Category.COMBAT,true,false,false, 755);
        INSTANCE = this;
    }

    public static Quiver getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        mc.options.useKey.setPressed(false);
        arrows.clear();
    }

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {
        int arrowSlot = -1;
        StatusEffectInstance statusEffect = null;
        for (int i = 0; i < 36; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof TippedArrowItem))
            {
                continue;
            }
            if (stack.getComponents().contains(DataComponentTypes.POTION_CONTENTS))
            {
                List<StatusEffectInstance> p = (List<StatusEffectInstance>) stack.getComponents()
                        .get(DataComponentTypes.POTION_CONTENTS).getEffects();
                for (StatusEffectInstance effect : p)
                {
                    StatusEffect type = effect.getEffectType().value();
                    if (type.isBeneficial() && !arrows.contains(effect))
                    {
                        arrowSlot = i;
                        statusEffect = effect;
                        break;
                    }
                }
            }

            if (arrowSlot != -1)
            {
                break;
            }
        }
        int bowSlot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.BOW)
            {
                bowSlot = i;
                break;
            }
        }
        float pullTime = BowItem.getPullProgress(pullTicks);
        if (mc.player.getMainHandStack().getItem() != Items.BOW || bowSlot == -1 || arrowSlot == -1)
        {
            disable();
            return;
        }
        setRotation(mc.player.getYaw(), -90.0f);
        if (arrowSlot != 9)
        {
            if (mc.player.currentScreenHandler.getCursorStack().getItem() != Items.TIPPED_ARROW)
            {
                mc.interactionManager.clickSlot(0, arrowSlot < 9 ? arrowSlot + 36 : arrowSlot, 0, SlotActionType.PICKUP, mc.player);
            }
            if (mc.player.currentScreenHandler.getCursorStack().getItem() == Items.TIPPED_ARROW)
            {
                mc.interactionManager.clickSlot(0, 9, 0, SlotActionType.PICKUP, mc.player);
            }
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            {
                mc.interactionManager.clickSlot(0, arrowSlot < 9 ? arrowSlot + 36 : arrowSlot, 0, SlotActionType.PICKUP, mc.player);
            }
        }
        if (pullTime >= 0.15f)
        {
            arrows.add(statusEffect);
            mc.options.useKey.setPressed(false);
            MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.stopUsingItem();
            pullTicks = 0;
        }
        else
        {
            mc.options.useKey.setPressed(true);
            pullTicks++;
        }
    }
}
