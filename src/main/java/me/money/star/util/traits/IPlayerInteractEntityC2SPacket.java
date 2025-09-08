package me.money.star.util.traits;

import me.money.star.util.network.InteractType;
import net.minecraft.entity.Entity;


/**
 *
 */
public interface IPlayerInteractEntityC2SPacket {
    /**
     * @return
     */
    Entity getEntity();

    /**
     * @return
     */
    InteractType getType();
}
