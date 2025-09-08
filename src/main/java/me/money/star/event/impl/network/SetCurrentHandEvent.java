package me.money.star.event.impl.network;

import me.money.star.event.Event;
import me.money.star.mixin.MixinClientPlayerEntity;
import me.money.star.util.traits.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * @author linus
 * @see MixinClientPlayerEntity
 * @since 1.0
 */
public class SetCurrentHandEvent extends Event implements Util {
    //
    private final Hand hand;

    public SetCurrentHandEvent(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStackInHand() {
        return mc.player.getStackInHand(hand);
    }
}
