package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.modules.render.Environment;
import me.money.star.client.modules.render.SkyColors;
import me.money.star.event.impl.SkyboxEvent;
import me.money.star.event.impl.world.AddEntityEvent;
import me.money.star.event.impl.world.RemoveEntityEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Shadow
    @Nullable
    public abstract Entity getEntityById(int id);

    /**
     * @param entity
     * @param ci
     */
    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void hookAddEntity(Entity entity, CallbackInfo ci) {
        AddEntityEvent addEntityEvent = new AddEntityEvent(entity);
        Util.EVENT_BUS.post(addEntityEvent);
    }

    /**
     *
     * @param entityId
     * @param removalReason
     * @param ci
     */
    @Inject(method = "removeEntity", at = @At(value = "HEAD"))
    private void hookRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = getEntityById(entityId);
        if (entity == null) {
            return;
        }
        RemoveEntityEvent addEntityEvent = new RemoveEntityEvent(entity, removalReason);
        Util.EVENT_BUS.post(addEntityEvent);
    }
    @Inject(method = "getSkyColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetSkyColor(Vec3d cameraPos, float tickDelta,
                                 CallbackInfoReturnable<Vec3d> cir)
    {
        SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
        Util.EVENT_BUS.post(skyboxEvent);
        if (skyboxEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColorVec());
        }
    }

    /**
     * @param tickDelta
     * @param cir
     */
    @Inject(method = "getCloudsColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetCloudsColor(float tickDelta,
                                    CallbackInfoReturnable<Vec3d> cir)
    {
        SkyboxEvent.Cloud skyboxEvent = new SkyboxEvent.Cloud();
        Util.EVENT_BUS.post(skyboxEvent);
        if (skyboxEvent.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColorVec());
        }
    }
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void getSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> info) {
        Color color = new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

        if (MoneyStar.moduleManager.getModuleByClass(SkyColors.class).isEnabled()) {
            info.setReturnValue(color.getRGB());
        }
    }

}
