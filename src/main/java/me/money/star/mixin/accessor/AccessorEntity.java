package me.money.star.mixin.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author linus
 * @see Entity
 * @since 1.0
 */
@Mixin(Entity.class)
public interface AccessorEntity {
    /**
     *
     */
    @Invoker("unsetRemoved")
    void hookUnsetRemoved();

    /**
     *
     * @param index
     * @param value
     */
    @Invoker("setFlag")
    void hookSetFlag(int index, boolean value);
    @Accessor("movementMultiplier")
    Vec3d getMovementMultiplier();

    @Accessor("movementMultiplier")
    void setMovementMultiplier(Vec3d val);
    @Accessor("dimensions")
    EntityDimensions getDimensions();
    @Mutable
    @Accessor("pos")
    void setPos(Vec3d pos);
}
