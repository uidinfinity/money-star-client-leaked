package me.money.star.event.impl.entity;

import me.money.star.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.world.event.GameEvent;

public class EntityGameEvent extends Event {
    private final GameEvent gameEvent;
    private final Entity entity;

    public EntityGameEvent(GameEvent gameEvent, Entity entity) {
        this.gameEvent = gameEvent;
        this.entity = entity;
    }

    public GameEvent getGameEvent() {
        return gameEvent;
    }

    public Entity getEntity() {
        return entity;
    }
}
