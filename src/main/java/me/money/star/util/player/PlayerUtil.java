package me.money.star.util.player;

import me.money.star.util.math.position.PositionUtil;
import me.money.star.util.traits.Util;
import net.fabricmc.fabric.mixin.client.gametest.ClientWorldAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * @author linus & xgraza
 * @since 1.0
 */
public final class PlayerUtil implements Util
{
    public static float getLocalPlayerHealth()
    {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    // from MC source
    public static int computeFallDamage(float fallDistance, float damageMultiplier)
    {
        if (mc.player.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE))
        {
            return 0;
        }
        else
        {
            final StatusEffectInstance statusEffectInstance = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST);
            final float f = statusEffectInstance == null ? 0.0F : (float) (statusEffectInstance.getAmplifier() + 1);
            return MathHelper.ceil((fallDistance - 3.0F - f) * damageMultiplier);
        }
    }

    public static boolean isHolding(final Item item)
    {
        ItemStack itemStack = mc.player.getMainHandStack();
        if (!itemStack.isEmpty() && itemStack.getItem() == item)
        {
            return true;
        }
        itemStack = mc.player.getOffHandStack();
        return !itemStack.isEmpty() && itemStack.getItem() == item;
    }

    public static boolean isHotbarKeysPressed()
    {
        for (KeyBinding binding : mc.options.hotbarKeys)
        {
            if (binding.isPressed())
            {
                return true;
            }
        }
        return false;
    }

    public static boolean inWeb(double expandBb)
    {
        for (BlockPos blockPos : PositionUtil.getAllInBox(mc.player.getBoundingBox().expand(expandBb)))
        {
            BlockState state = mc.world.getBlockState(blockPos);
            if (state.getBlock() instanceof CobwebBlock)
            {
                return true;
            }
        }
        return false;
    }
}
