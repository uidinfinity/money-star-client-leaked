package me.money.star.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.commands.Command;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.world.StayCamera;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.MouseClickEvent;
import me.money.star.util.player.RayCastUtil;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;


public class MiddleClick extends Module {

    //

    public Setting<Boolean> friend = bool("Friend", true);
    public Setting<Boolean> pearl = bool("Pearl", false);
    public Setting<Boolean> firework = bool("Firework", false);
    public MiddleClick() {
        super("MiddleClick", "Adds an additional bind on the mouse middle button",
                Category.MISC,true,false,false);
    }
    @Subscribe
    public void onMouseClick(MouseClickEvent event)
    {
        Entity targetedEntity = Util.mc.targetedEntity;
        String name = ((PlayerEntity) targetedEntity).getGameProfile().getName();
        if (mc.player == null || mc.interactionManager == null)
        {
            return;
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == GLFW.GLFW_PRESS && mc.currentScreen == null)
        {
            double d = mc.player.getEntityInteractionRange();
            HitResult result = StayCamera.getInstance().isEnabled() ? RayCastUtil.raycastEntity(d,
                    StayCamera.getInstance().getCameraPosition(), StayCamera.getInstance().getCameraRotations()) : RayCastUtil.raycastEntity(d);
            if (result != null && result.getType() == HitResult.Type.ENTITY
                    && friend.getValue() && ((EntityHitResult) result).getEntity() instanceof PlayerEntity target)
            {
                if (MoneyStar.friendManager.isFriend(name))
                {
                    MoneyStar.friendManager.removeFriend(name);
                    Command.sendMessage(Formatting.RED + name + Formatting.RED + " has been unfriended.");
                }
                else
                {
                    MoneyStar.friendManager.addFriend(name);
                    Command.sendMessage(Formatting.AQUA + name + Formatting.AQUA + " has been friended.");
                }
            }
            else
            {
                Item item = null;
                if (mc.player.isGliding() && firework.getValue())
                {
                    item = Items.FIREWORK_ROCKET;
                }
                else if (pearl.getValue())
                {
                    item = Items.ENDER_PEARL;
                }
                if (item == null)
                {
                    return;
                }
                int slot = -1;
                for (int i = 0; i < 45; i++)
                {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack.getItem() == item)
                    {
                        slot = i;
                        break;
                    }
                }

                if (slot == -1)
                {
                    return;
                }

                if (slot < 9)
                {
                    MoneyStar.inventoryManager.setSlot(slot);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    MoneyStar.inventoryManager.syncToClient();
                }
                else
                {
                    mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
    }
}
