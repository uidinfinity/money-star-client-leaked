package me.money.star.mixin;

import me.money.star.event.impl.RenderFirstPersonEvent;
import me.money.star.event.impl.render.item.RenderArmEvent;
import me.money.star.util.traits.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     *
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param arm
     * @param ci
     */
    @Inject(method = "renderArmHoldingItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) entityRenderDispatcher.getRenderer(client.player);
        RenderArmEvent renderArmEvent = new RenderArmEvent(matrices, vertexConsumers, light, equipProgress, swingProgress, arm, playerEntityRenderer);
        Util.EVENT_BUS.post(renderArmEvent);
        if (renderArmEvent.isCancelled()) {
            ci.cancel();
        }
    }
    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderFirstPersonItem$2(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
    {
        RenderFirstPersonEvent.Head renderFirstPersonEvent = new RenderFirstPersonEvent.Head(hand, item, equipProgress, matrices);
        Util.EVENT_BUS.post(renderFirstPersonEvent);
        if (renderFirstPersonEvent.isCancelled())
        {
            ci.cancel();
        }
    }




//    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "HEAD"), cancellable = true)
//    private void hookApplyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, CallbackInfo ci) {
//        ci.cancel();
//        float h;
//        float f = (float) client.player.getItemUseTimeLeft() - tickDelta + 1.0f;
//        float g = f / (float)stack.getMaxUseTime();
//        if (g < 0.8f) {
//            h = MathHelper.abs(MathHelper.cos(f / 4.0f * (float)Math.PI) * 0.1f);
//            matrices.translate(0.0f, h, 0.0f);
//        }
//        h = 1.0f - (float) Math.pow(g, 27.0);
//        int i = arm == Arm.RIGHT ? 1 : -1;
//        matrices.translate(h * 0.6f * (float)i, h * -0.5f, h * 0.0f);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * h * 90.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0f));
//        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * 30.0f));
//    }
}
