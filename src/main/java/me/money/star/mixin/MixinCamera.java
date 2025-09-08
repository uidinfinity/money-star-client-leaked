package me.money.star.mixin;

import me.money.star.event.impl.camera.CameraPositionEvent;
import me.money.star.event.impl.camera.CameraRotationEvent;
import me.money.star.event.impl.gui.hud.RenderOverlayEvent;
import me.money.star.event.impl.render.CameraClipEvent;
import me.money.star.util.traits.Util;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow private float lastTickDelta;

    @Shadow protected abstract void setPos(double x, double y, double z);

    @Shadow protected abstract void setRotation(float yaw, float pitch);

    /**
     * @param cir
     */
    @Inject(method = "getSubmersionType", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
        RenderOverlayEvent.Water renderOverlayEvent =
                new RenderOverlayEvent.Water(null);
        Util.EVENT_BUS.post(renderOverlayEvent);
        if (renderOverlayEvent.isCancelled()) {
            cir.setReturnValue(CameraSubmersionType.NONE);
            cir.cancel();
        }
    }
    @Inject(method = "clipToSpace", at = @At(value = "HEAD"), cancellable = true)
    private void hookClipToSpace(float f, CallbackInfoReturnable<Float> cir)
    {
        CameraClipEvent cameraClipEvent =
                new CameraClipEvent(f);
        Util.EVENT_BUS.post(cameraClipEvent);
        if (cameraClipEvent.isCancelled())
        {
            cir.setReturnValue(cameraClipEvent.getDistance());
            cir.cancel();
        }
    }



    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void hookUpdatePosition(Camera instance, double x, double y, double z) {
        CameraPositionEvent cameraPositionEvent = new CameraPositionEvent(x, y, z, lastTickDelta);
        Util.EVENT_BUS.post(cameraPositionEvent);
        setPos(cameraPositionEvent.getX(), cameraPositionEvent.getY(), cameraPositionEvent.getZ());

    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void hookUpdateRotation(Camera instance, float yaw, float pitch) {
        CameraRotationEvent cameraRotationEvent = new CameraRotationEvent(yaw, pitch, lastTickDelta);
        Util.EVENT_BUS.post(cameraRotationEvent);
        setRotation(cameraRotationEvent.getYaw(), cameraRotationEvent.getPitch());
    }
}
