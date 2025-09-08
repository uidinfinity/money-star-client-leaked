package me.money.star.event.impl.entity.projectile;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.projectile.FireworkRocketEntity;

@Cancelable
public class RemoveFireworkEvent extends Event {
    private final FireworkRocketEntity rocketEntity;

    public RemoveFireworkEvent(FireworkRocketEntity rocketEntity) {
        this.rocketEntity = rocketEntity;
    }

    public FireworkRocketEntity getRocketEntity() {
        return rocketEntity;
    }
}
