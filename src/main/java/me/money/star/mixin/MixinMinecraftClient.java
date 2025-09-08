package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.event.Stage;
import me.money.star.event.impl.entity.EntityDeathEvent;
import me.money.star.client.modules.world.NoCooldown;
import me.money.star.util.traits.IMinecraftClient;
import me.money.star.util.traits.Util;
import me.money.star.event.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;
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
 * @since 1.0
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClient {
    //
    @Shadow
    public ClientWorld world;
    //
    @Shadow
    public ClientPlayerEntity player;
    //
    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    //
    @Shadow
    protected int attackCooldown;
    @Unique
    private boolean leftClick;
    // https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/mixin/MinecraftClientMixin.java#L54
    @Unique
    private boolean rightClick;
    @Unique
    private boolean doAttackCalled;
    @Unique
    private boolean doItemUseCalled;

    /**
     *
     */
    @Shadow
    protected abstract void doItemUse();

    /**
     * @return
     */
    @Shadow
    protected abstract boolean doAttack();

    /**
     *
     */
    @Override
    public void leftClick() {
        leftClick = true;
    }

    /**
     *
     */
    @Override
    public void rightClick() {
        rightClick = true;
    }

    /**
     * @param ci
     */
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.BEFORE))
    private void hookRun(CallbackInfo ci) {
        final RunTickEvent runTickEvent = new RunTickEvent();
        Util.EVENT_BUS.post(runTickEvent);
    }

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTickPre(CallbackInfo ci) {
        doAttackCalled = false;
        doItemUseCalled = false;
        if (player != null && world != null) {
            TickEvent tickPreEvent = new TickEvent();
            tickPreEvent.setStage(Stage.PRE);
            Util.EVENT_BUS.post(tickPreEvent);
        }
        if (interactionManager == null) {
            return;
        }
        if (leftClick && !doAttackCalled) {
            doAttack();
        }
        if (rightClick && !doItemUseCalled) {
            doItemUse();
        }
        leftClick = false;
        rightClick = false;
    }

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void hookTickPost(CallbackInfo ci) {
        if (player != null && world != null) {
            TickEvent tickPostEvent = new TickEvent();
            tickPostEvent.setStage(Stage.POST);
            Util.EVENT_BUS.post(tickPostEvent);
            world.getEntities().forEach(entity -> {
                if (entity instanceof LivingEntity e && e.isDead()) {
                    EntityDeathEvent entityDeathEvent = new EntityDeathEvent(e);
                    Util.EVENT_BUS.post(entityDeathEvent);
                }
            });
        }
    }

    /**
     * @param screen
     * @param ci
     */
    @Inject(method = "setScreen", at = @At(value = "TAIL"))
    private void hookSetScreen(Screen screen, CallbackInfo ci) {
        ScreenOpenEvent screenOpenEvent = new ScreenOpenEvent(screen);
        Util.EVENT_BUS.post(screenOpenEvent);
    }

    /**
     * @param ci
     */
    @Inject(method = "doItemUse", at = @At(value = "HEAD"))
    private void hookDoItemUse(CallbackInfo ci) {
        doItemUseCalled = true;
    }

    /**
     * @param cir
     */
    @Inject(method = "doAttack", at = @At(value = "HEAD"))
    private void hookDoAttack(CallbackInfoReturnable<Boolean> cir) {
        doAttackCalled = true;
        AttackCooldownEvent attackCooldownEvent = new AttackCooldownEvent();
        Util.EVENT_BUS.post(attackCooldownEvent);
        if (attackCooldownEvent.isCancelled()) {
            attackCooldown = 0;
        }
    }

    /**
     * @param instance
     * @return
     */
    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean hookIsUsingItem(ClientPlayerEntity instance) {
        ItemMultitaskEvent itemMultitaskEvent = new ItemMultitaskEvent();
        Util.EVENT_BUS.post(itemMultitaskEvent);
        return !itemMultitaskEvent.isCancelled() && instance.isUsingItem();
    }

    /**
     * @param instance
     * @return
     */
    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    private boolean hookIsBreakingBlock(ClientPlayerInteractionManager instance) {
        ItemMultitaskEvent itemMultitaskEvent = new ItemMultitaskEvent();
        Util.EVENT_BUS.post(itemMultitaskEvent);
        return !itemMultitaskEvent.isCancelled() && instance.isBreakingBlock();
    }




    /**
     * @param entity
     * @param cir
     */
    @Inject(method = "hasOutline", at = @At(value = "HEAD"), cancellable = true)
    private void hookHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityOutlineEvent entityOutlineEvent = new EntityOutlineEvent(entity);
        Util.EVENT_BUS.post(entityOutlineEvent);
        if (entityOutlineEvent.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> info) {
        if (MoneyStar.moduleManager != null && MoneyStar.moduleManager.getModuleByClass(NoCooldown.class).isEnabled() && MoneyStar.moduleManager.getModuleByClass(NoCooldown.class).hitDelay.getValue() ) {
            attackCooldown = 0;
        }
    }


}
