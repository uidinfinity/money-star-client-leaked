package me.money.star.event.impl.particle;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class ParticleEvent extends Event {
    //
    private final ParticleEffect particle;

    /**
     * @param particle
     */
    public ParticleEvent(ParticleEffect particle) {
        this.particle = particle;
    }

    /**
     * @return
     */
    public ParticleEffect getParticle() {
        return particle;
    }

    /**
     * @return
     */
    public ParticleType<?> getParticleType() {
        return particle.getType();
    }

    @Cancelable
    public static class Emitter extends ParticleEvent {
        /**
         * @param particle
         */
        public Emitter(ParticleEffect particle) {
            super(particle);
        }
    }
}
