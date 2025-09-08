package me.money.star.mixin;

import me.money.star.event.impl.particle.ParticleEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ParticleManager.class)
public class MixinParticleManager {
    /**
     * @param parameters
     * @param x
     * @param y
     * @param z
     * @param velocityX
     * @param velocityY
     * @param velocityZ
     * @param cir
     */
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;" +
            "DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At(value =
            "HEAD"), cancellable = true)
    private void hookAddParticle(ParticleEffect parameters, double x,
                                 double y, double z, double velocityX,
                                 double velocityY, double velocityZ,
                                 CallbackInfoReturnable<Particle> cir) {
        ParticleEvent particleEvent = new ParticleEvent(parameters);
        Util.EVENT_BUS.post(particleEvent);
        if (particleEvent.isCancelled()) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    /**
     * @param entity
     * @param parameters
     * @param maxAge
     * @param ci
     */
    @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft" +
            "/particle/ParticleEffect;I)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookAddEmitter(Entity entity, ParticleEffect parameters,
                                int maxAge, CallbackInfo ci) {
        ParticleEvent.Emitter particleEvent =
                new ParticleEvent.Emitter(parameters);
        Util.EVENT_BUS.post(particleEvent);
        if (particleEvent.isCancelled()) {
            ci.cancel();
        }
    }
}
