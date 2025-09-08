package me.money.star.util.world;

import me.money.star.util.traits.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import java.util.function.BiFunction;

/**
 * @author linus
 * @since 1.0
 */
public class EndCrystalUtil implements Util {
    /**
     * @param entity
     * @param crystal
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d crystal) {
        return getDamageTo(entity, crystal, false);
    }

    /**
     * @param entity
     * @param crystal
     * @param ignoreTerrain
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d crystal,
                                     final boolean ignoreTerrain) {
        double ab = getExposure(crystal, entity, ignoreTerrain);
        double w = Math.sqrt(entity.squaredDistanceTo(crystal)) / 12.0;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    /**
     * @param pos
     * @param entity
     * @param crystal
     * @return
     */
    public static double getDamageToPos(final Vec3d pos,
                                        final Entity entity,
                                        final Vec3d crystal) {
        return getDamageToPos(pos, entity, crystal, false);
    }

    /**
     * @param pos           The actual position of the damage
     * @param entity
     * @param crystal
     * @param ignoreTerrain
     * @return
     */
    public static double getDamageToPos(final Vec3d pos,
                                        final Entity entity,
                                        final Vec3d crystal,
                                        final boolean ignoreTerrain) {
        final Box bb = entity.getBoundingBox();
        double dx = pos.getX() - bb.minX;
        double dy = pos.getY() - bb.minY;
        double dz = pos.getZ() - bb.minZ;
        final Box box = bb.offset(dx, dy, dz);
        //
        double ab = getExposure(crystal, box, ignoreTerrain);
        double w = Math.sqrt(pos.squaredDistanceTo(crystal)) / 12.0;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    /**
     * @param entity
     * @param damage
     * @return
     */
    private static double getReduction(Entity entity, DamageSource damageSource, double damage) {
        if (damageSource.isScaledWithDifficulty()) {
            switch (mc.world.getDifficulty()) {
                // case PEACEFUL -> return 0;
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }
        if (entity instanceof LivingEntity livingEntity) {
            //damage = DamageUtil.getDamageLeft((float) damage, getArmor(livingEntity), (float) getAttributeValue(livingEntity, EntityAttributes.);
            //damage = getProtectionReduction(entity, damage, damageSource);
        }
        return Math.max(damage, 0);
    }
/*
    private static float getArmor(LivingEntity entity) {
        return (float) Math.floor(getAttributeValue(entity, EntityAttributes.ARMOR.value()));
    }

 */
/*
    private static float getProtectionReduction(Entity player, double damage, DamageSource source) {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.p, source);
        return DamageUtil.getInflictedDamage((float) damage, protLevel);
    }

 */

    /*
    public static double getAttributeValue(LivingEntity entity, EntityAttribute attribute) {
        return getAttributeInstance(entity, attribute).getValue();
    }

     */
/*
    public static EntityAttributeInstance getAttributeInstance(LivingEntity entity, EntityAttribute attribute) {
        double baseValue = getDefaultForEntity(entity).getBaseValue(attribute);
        EntityAttributeInstance attributeInstance = new EntityAttributeInstance(attribute, o1 -> {
        });
        attributeInstance.setBaseValue(baseValue);
        for (var equipmentSlot : EquipmentSlot.values()) {
            ItemStack stack = entity.getEquippedStack(equipmentSlot);
            Multimap<EntityAttribute, EntityAttributeModifier> modifiers = stack.getAttributeModifiers(equipmentSlot);
            for (var modifier : modifiers.get(attribute)) attributeInstance.addTemporaryModifier(modifier);
        }
        return attributeInstance;
    }


 */
    private static <T extends LivingEntity> DefaultAttributeContainer getDefaultForEntity(T entity) {
        return DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getType());
    }

    /**
     * @param source
     * @param entity
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Entity entity,
                                     final boolean ignoreTerrain) {
        final Box box = entity.getBoundingBox();
        return getExposure(source, box, ignoreTerrain);
    }

    /**
     * @param source
     * @param box
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Box box,
                                     final boolean ignoreTerrain) {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain);

        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1 / (xDiff * 2 + 1);
        double yStep = 1 / (yDiff * 2 + 1);
        double zStep = 1 / (zDiff * 2 + 1);

        if (xStep > 0 && yStep > 0 && zStep > 0) {
            int misses = 0;
            int hits = 0;

            double xOffset = (1 - Math.floor(1 / xStep) * xStep) * 0.5;
            double zOffset = (1 - Math.floor(1 / zStep) * zStep) * 0.5;

            xStep = xStep * xDiff;
            yStep = yStep * yDiff;
            zStep = zStep * zDiff;

            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;

            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3d position = new Vec3d(x, y, z);

                        if (raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) misses++;

                        hits++;
                    }
                }
            }

            return (float) misses / hits;
        }

        return 0f;
    }

    private static RaycastFactory getRaycastFactory(boolean ignoreTerrain) {
        if (ignoreTerrain) {
            return (context, blockPos) -> {
                BlockState blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600) return null;

                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        } else {
            return (context, blockPos) -> {
                BlockState blockState = mc.world.getBlockState(blockPos);
                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
    }

    /* Raycasts */

    private static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory) {
        return BlockView.raycast(context.start, context.end, context, raycastFactory, ctx -> null);
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end) {}

    @FunctionalInterface
    public interface RaycastFactory extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {}
}
