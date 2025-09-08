package me.money.star.event.impl.biome;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.world.biome.BiomeParticleConfig;


@Cancelable
public class BiomeEffectsEvent extends Event {

    private BiomeParticleConfig particleConfig;

    public BiomeParticleConfig getParticleConfig() {
        return particleConfig;
    }

    public void setParticleConfig(BiomeParticleConfig particleConfig) {
        this.particleConfig = particleConfig;
    }
}
