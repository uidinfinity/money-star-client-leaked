package me.money.star.mixin.accessor;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

/**
 *
 */
@Mixin(UpdateBeaconC2SPacket.class)
public interface AccessorUpdateBeaconC2SPacket {

}
