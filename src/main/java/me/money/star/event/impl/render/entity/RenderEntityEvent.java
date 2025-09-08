package me.money.star.event.impl.render.entity;

import me.money.star.event.Cancelable;
import me.money.star.event.Event;
import net.minecraft.entity.LivingEntity;

@Cancelable
public class RenderEntityEvent<T extends LivingEntity> extends Event {

}
