package me.money.star.mixin.accessor;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(PlayerMoveC2SPacket.class)
public interface AccessorPlayerMoveC2SPacket {
    /**
     * @param onGround
     */
    @Accessor("onGround")
    @Mutable
    void hookSetOnGround(boolean onGround);

    /**
     * @param x
     */
    @Accessor("x")
    @Mutable
    void hookSetX(double x);

    /**
     * @param y
     */
    @Accessor("y")
    @Mutable
    void hookSetY(double y);

    /**
     * @param z
     */
    @Accessor("z")
    @Mutable
    void hookSetZ(double z);

    /**
     * @param yaw
     */
    @Accessor("yaw")
    @Mutable
    void hookSetYaw(float yaw);

    /**
     * @param pitch
     */
    @Accessor("pitch")
    @Mutable
    void hookSetPitch(float pitch);
    @Accessor("yaw") @Mutable
    void setYaw(float yaw);

    @Accessor("pitch") @Mutable
    void setPitch(float pitch);

    @Accessor("onGround") @Mutable
    void setOnGround(boolean onGround);

    @Accessor("changeLook") @Mutable
    void setChangeLook(boolean changeLook);
}
