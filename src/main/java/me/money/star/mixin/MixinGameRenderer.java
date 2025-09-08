package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.client.modules.render.Aspect;
import me.money.star.event.impl.RenderNauseaEvent;
import me.money.star.event.impl.render.BobViewEvent;
import me.money.star.event.impl.render.FovEvent;
import me.money.star.event.impl.render.HurtCamEvent;
import me.money.star.client.modules.world.NoHitBox;
import me.money.star.util.traits.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @see GameRenderer
 * @since 1.0
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer implements Util {
    //
    @Shadow
    @Final
    MinecraftClient client;

    @Shadow
    private float lastFovMultiplier;

    @Shadow
    private float fovMultiplier;
    @Shadow private float zoom;

    @Shadow private float zoomX;

    @Shadow private float zoomY;
    @Shadow private float viewDistance;





    /**
     * @param matrices
     * @param tickDelta
     * @param ci
     */
    @Inject(method = "tiltViewWhenHurt", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookTiltViewWhenHurt(MatrixStack matrices, float tickDelta,
                                      CallbackInfo ci) {
        HurtCamEvent hurtCamEvent = new HurtCamEvent();
        Util.EVENT_BUS.post(hurtCamEvent);
        if (hurtCamEvent.isCancelled()) {
            ci.cancel();
        }
    }


    /**
     * @param matrices
     * @param tickDelta
     * @param ci
     */
    @Inject(method = "bobView", at = @At(value = "HEAD"), cancellable = true)
    private void hookBobView(MatrixStack matrices, float tickDelta,
                             CallbackInfo ci) {
        BobViewEvent bobViewEvent = new BobViewEvent();
        Util.EVENT_BUS.post(bobViewEvent);
        if (bobViewEvent.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     *
     * @param camera
     * @param tickDelta
     * @param changingFov
     * @param cir
     */
    @Inject(method = "getFov", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        FovEvent fovEvent = new FovEvent();
        Util.EVENT_BUS.post(fovEvent);
        if (fovEvent.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(fovEvent.getFov() * (double) MathHelper.lerp(tickDelta, lastFovMultiplier, fovMultiplier));
        }
    }
    @Inject(method = "findCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void findCrosshairTargetHook(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        if (MoneyStar.moduleManager.getModuleByClass(NoHitBox.class).isEnabled() && (mc.player.getMainHandStack().getItem() instanceof PickaxeItem || !MoneyStar.moduleManager.getModuleByClass(NoHitBox.class).ponly.getValue())) {
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem && MoneyStar.moduleManager.getModuleByClass(NoHitBox.class).noSword.getValue()) return;
            double d = Math.max(blockInteractionRange, entityInteractionRange);
            Vec3d vec3d = camera.getCameraPosVec(tickDelta);
            HitResult hitResult = camera.raycast(d, tickDelta, false);
            cir.setReturnValue(ensureTargetInRangeCustom(hitResult, vec3d, blockInteractionRange));
        }
    }
    @Unique
    private HitResult ensureTargetInRangeCustom(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        Vec3d vec3d = hitResult.getPos();
        if (!vec3d.isInRange(cameraPos, interactionRange)) {
            Vec3d vec3d2 = hitResult.getPos();
            Direction direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
        } else {
            return hitResult;
        }
    }

    @Inject(method = "getBasicProjectionMatrix",at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrix(float fovDegrees, CallbackInfoReturnable<Matrix4f> info) {
        if (MoneyStar.moduleManager.getModuleByClass(Aspect.class).isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }

            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float)(fovDegrees * 0.01745329238474369), (MoneyStar.moduleManager.getModuleByClass(Aspect.class).ratio.getValue()), 0.05f, viewDistance * 4.0f));
            info.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float hookLerpNausea(float delta, float start, float end)
    {
        RenderNauseaEvent renderNauseaEvent = new RenderNauseaEvent();
        Util.EVENT_BUS.post(renderNauseaEvent);
        if (renderNauseaEvent.isCancelled())
        {
            return 0.0f;
        }
        return MathHelper.lerp(delta, start, end);
    }

}
