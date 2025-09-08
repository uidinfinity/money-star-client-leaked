package me.money.star.mixin;

import me.money.star.event.impl.item.FireworkUseEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(FireworkRocketItem.class)
public class MixinFireworkRocketItem {
    /**
     * @param world
     * @param user
     * @param hand
     * @param cir
     */
    @Inject(method = "use", at = @At(value = "HEAD"))
    private void hookUse(World world, PlayerEntity user, Hand hand,
                         CallbackInfoReturnable<ActionResult> cir) {
        FireworkUseEvent fireworkUseEvent = new FireworkUseEvent();
        Util.EVENT_BUS.post(fireworkUseEvent);
    }
}
