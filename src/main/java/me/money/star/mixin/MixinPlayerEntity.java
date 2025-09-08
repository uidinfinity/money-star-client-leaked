package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.event.Stage;
import me.money.star.event.impl.entity.player.PlayerJumpEvent;
import me.money.star.event.impl.entity.player.PushFluidsEvent;
import me.money.star.event.impl.entity.player.TravelEvent;
import me.money.star.client.modules.movement.SafeWalk;
import me.money.star.util.traits.Util;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements Util {
    /**
     * @param entityType
     * @param world
     */
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract void travel(Vec3d movementInput);

    /**
     * @param movementInput
     * @param ci
     */
    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravelHead(Vec3d movementInput, CallbackInfo ci) {
        TravelEvent travelEvent = new TravelEvent(movementInput);
        travelEvent.setStage(Stage.PRE);
        Util.EVENT_BUS.post(travelEvent);
        if (travelEvent.isCancelled()) {
            move(MovementType.SELF, getVelocity());
            ci.cancel();
        }
    }


    /**
     * @param movementInput
     * @param ci
     */
    @Inject(method = "travel", at = @At(value = "RETURN"), cancellable = true)
    private void hookTravelTail(Vec3d movementInput, CallbackInfo ci) {
        TravelEvent travelEvent = new TravelEvent(movementInput);
        travelEvent.setStage(Stage.POST);
        Util.EVENT_BUS.post(travelEvent);
    }
    /*
    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJumpPre(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
        playerJumpEvent.setStage(Stage.PRE);
        Util.EVENT_BUS.post(playerJumpEvent);
        if (playerJumpEvent.isCancelled())
        {
            ci.cancel();
        }
    }

     */
    /*
    @Inject(method = "jump", at = @At(value = "RETURN"), cancellable = true)
    private void hookJumpPost(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
        playerJumpEvent.setStage(Stage.POST);
        Util.EVENT_BUS.post(playerJumpEvent);
    }

     */

    /**
     * @param cir
     */
    @Inject(method = "isPushedByFluids", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookIsPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != mc.player) {
            return;
        }
        PushFluidsEvent pushFluidsEvent = new PushFluidsEvent();
        Util.EVENT_BUS.post(pushFluidsEvent);
        if (pushFluidsEvent.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        if (MoneyStar.moduleManager.getModuleByClass(SafeWalk.class).isEnabled()) {
            info.setReturnValue(true);
        }
    }

}
