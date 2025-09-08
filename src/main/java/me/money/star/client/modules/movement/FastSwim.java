package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.System;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.player.PlayerMoveEvent;
import me.money.star.event.impl.network.TickMovementEvent;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.EnchantmentUtil;
import me.money.star.util.traits.Util;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;

public class FastSwim extends Module {

    //
    public Setting<SwimMode> mode = mode("Mode", SwimMode.VANILLA);
    public Setting<Float>  waterSpeed = num("WaterSpeed", 1.0f, 3.0f, 10.0f);
    public Setting<Float> lavaSpeed = num("LavaSpeed", 1.0f, 3.0f, 10.0f);
    public Setting<Boolean>  vertical = bool("Vertical", true);
    public Setting<Boolean> elytra = bool("Elytra", true);
    public Setting<Float> elytraSpeed = num("ElytraSpeed", 1.0f, 3.0f, 10.0f);
    public Setting<Boolean> depthStrider = bool("DepthStrider", true);

    public FastSwim() {
        super("FastSwim", "Falls down blocks faster", Category.MOVEMENT,true,false,false);
    }

    @Subscribe
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!depthStrider.getValue() && EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.DEPTH_STRIDER) > 0)
        {
            return;
        }
        // Must be fully submerged to apply speed
        if (!mc.player.isSubmergedIn(FluidTags.WATER) && !mc.player.isSubmergedIn(FluidTags.LAVA))
        {
            return;
        }
        event.cancel();
        float speed = mc.player.isSubmergedIn(FluidTags.WATER) ? waterSpeed.getValue() : lavaSpeed.getValue();
        switch (mode.getValue())
        {
            case VANILLA ->
            {
                event.setX(event.getX() * speed);
                event.setZ(event.getZ() * speed);
            }
            case NORMAL ->
            {
                Vec2f strafe = Speed.getInstance().handleStrafeMotion(speed / 10.0f);
                event.setX(strafe.x);
                event.setZ(strafe.y);
            }
        }
        if (vertical.getValue())
        {
            if (mc.options.jumpKey.isPressed())
            {
                event.setY(event.getY() + 0.16);
                MoneyStar.movementManager.setMotionY(event.getY() + 0.16);
            }
            else if (mc.options.sneakKey.isPressed())
            {
                event.setY(event.getY() - 0.12);
                MoneyStar.movementManager.setMotionY(event.getY() - 0.12);
            }
        }
    }

    private enum SwimMode
    {
        VANILLA,
        NORMAL
    }
}