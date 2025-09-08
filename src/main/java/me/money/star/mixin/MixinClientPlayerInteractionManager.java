package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.event.impl.ItemDesyncEvent;
import me.money.star.event.impl.SyncSelectedSlotEvent;
import me.money.star.event.impl.network.AttackBlockEvent;
import me.money.star.event.impl.network.BreakBlockEvent;
import me.money.star.event.impl.network.InteractBlockEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @see ClientPlayerInteractionManager
 * @since 1.0
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements Util
{
    @Shadow
    private GameMode gameMode;

    @Shadow
    protected abstract void syncSelectedSlot();

    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    /**
     * @param pos
     * @param direction
     * @param cir
     */
    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookAttackBlock(BlockPos pos, Direction direction,
                                 CallbackInfoReturnable<Boolean> cir)
    {
        BlockState state = mc.world.getBlockState(pos);
        final AttackBlockEvent attackBlockEvent = new AttackBlockEvent(
                pos, state, direction);
        Util.EVENT_BUS.post(attackBlockEvent);
        if (attackBlockEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }

    /**
     * @param player
     * @param hand
     * @param hitResult
     * @param cir
     */
    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookInteractBlock(ClientPlayerEntity player, Hand hand,
                                   BlockHitResult hitResult,
                                   CallbackInfoReturnable<ActionResult> cir)
    {
        InteractBlockEvent interactBlockEvent = new InteractBlockEvent(
                player, hand, hitResult);
        Util.EVENT_BUS.post(interactBlockEvent);
        if (interactBlockEvent.isCancelled())
        {
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
    }



    /**
     * @param pos
     * @param cir
     */
    @Inject(method = "breakBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        BreakBlockEvent breakBlockEvent = new BreakBlockEvent(pos);
        Util.EVENT_BUS.post(breakBlockEvent);
        if (breakBlockEvent.isCancelled())
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Redirect(
            method = "interactBlockInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack hookRedirectInteractBlockInternal$getStackInHand(ClientPlayerEntity entity, Hand hand)
    {
        if (hand.equals(Hand.OFF_HAND))
        {
            return entity.getStackInHand(hand);
        }
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        Util.EVENT_BUS.post(itemDesyncEvent);
        return itemDesyncEvent.isCancelled() ? itemDesyncEvent.getServerItem() : entity.getStackInHand(Hand.MAIN_HAND);
    }

    @Redirect(
            method = "interactBlockInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
                    ordinal = 0))
    private boolean hookRedirectInteractBlockInternal$getMainHandStack(ItemStack instance)
    {
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        Util.EVENT_BUS.post(itemDesyncEvent);
        return itemDesyncEvent.isCancelled() ? itemDesyncEvent.getServerItem().isEmpty() : instance.isEmpty();
    }

    @Inject(method = "syncSelectedSlot", at = @At(value = "HEAD"), cancellable = true)
    private void hookSyncSelectedSlot(CallbackInfo ci)
    {
        SyncSelectedSlotEvent syncSelectedSlotEvent = new SyncSelectedSlotEvent();
        Util.EVENT_BUS.post(syncSelectedSlotEvent);
        if (syncSelectedSlotEvent.isCancelled())
        {
            ci.cancel();
        }
    }
}   