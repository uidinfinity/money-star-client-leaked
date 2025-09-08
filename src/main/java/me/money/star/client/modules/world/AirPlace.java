package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.traits.Util;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;



public final class AirPlace extends Module
{

    public Setting<Float> range = num("Range ", 4.0f, 1.0f, 10.0f);
    public Setting<Boolean> fluids = bool("Fluids", false);

    public AirPlace()
    {
        super("AirPlace", "Allows you to place blocks in the air", Category.WORLD,true,false,false);
    }

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {
        final ItemStack stack = Util.mc.player.getMainHandStack();
        if ((stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) || !Util.mc.options.useKey.isPressed())
        {
            return;
        }

        final HitResult result = Util.mc.player.raycast(range.getValue(), 1.0f, fluids.getValue());
        if (result instanceof BlockHitResult blockHitResult)
        {
            final ActionResult actionResult = Util.mc.interactionManager.interactBlock(Util.mc.player, Hand.MAIN_HAND, blockHitResult);
            if (actionResult.isAccepted() )
            {
                Util.mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
