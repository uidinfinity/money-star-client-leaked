/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.client.modules.render.FullBright;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class MixinLightmapTextureManager {
    @Shadow
    @Final
    private SimpleFramebuffer lightmapFramebuffer;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V", shift = At.Shift.BEFORE))
    private void update$endWrite(float delta, CallbackInfo info) {
        if (MoneyStar.moduleManager != null && MoneyStar.moduleManager.getModuleByClass(FullBright.class).isEnabled() && MoneyStar.moduleManager.getModuleByClass(FullBright.class).mode.getValue() == FullBright.Mode.GAMMA) {
            lightmapFramebuffer.clear();
        }
    }
}