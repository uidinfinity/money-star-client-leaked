package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.event.impl.entity.ConsumeItemEvent;
import me.money.star.event.impl.entity.JumpRotationEvent;
import me.money.star.client.modules.world.NoCooldown;
import me.money.star.util.traits.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends MixinEntity implements Util {
    //
    @Shadow
    protected ItemStack activeItemStack;


    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> par1);


    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    protected abstract float getJumpVelocity();

    @Shadow
    public abstract boolean isDead();

    @Shadow
    public int deathTime;

    @Shadow private int jumpingCooldown;

    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJump$getYaw(CallbackInfo ci) {
        if ((LivingEntity) (Object) this != mc.player) {
            return;
        }
        final JumpRotationEvent event = new JumpRotationEvent();
        Util.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            Vec3d vec3d = this.getVelocity();
            setVelocity(new Vec3d(vec3d.x, getJumpVelocity(), vec3d.z));
            if (isSprinting()) {
                float f = event.getYaw() * ((float)Math.PI / 180);
                setVelocity(getVelocity().add(-MathHelper.sin(f) * 0.2f, 0.0, MathHelper.cos(f) * 0.2f));
            }
            velocityDirty = true;
        }
    }




    /**
     * @param ci
     */
    @Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;" +
            "Lnet/minecraft/entity/LivingEntity;)" +
            "Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void hookConsumeItem(CallbackInfo ci) {
        if ((Object) this != mc.player) {
            return;
        }
        ConsumeItemEvent consumeItemEvent = new ConsumeItemEvent(activeItemStack);
        Util.EVENT_BUS.post(consumeItemEvent);
    }


    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 2, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {
        if (MoneyStar.moduleManager != null && MoneyStar.moduleManager.getModuleByClass(NoCooldown.class).isEnabled()&& MoneyStar.moduleManager.getModuleByClass(NoCooldown.class).jumpDelay.getValue() && jumpingCooldown == 10) {
            jumpingCooldown = 0;
        }
    }
}
