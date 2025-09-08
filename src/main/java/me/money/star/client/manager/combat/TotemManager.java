package me.money.star.client.manager.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.entity.EntityDeathEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class TotemManager implements Util {
    //
    private final Set<Object> subscribers = Collections.synchronizedSet(new HashSet<>());
    private final ConcurrentMap<UUID, Integer> totems = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, TotemData> totems2 = new ConcurrentHashMap<>();


    /**
     *
     */
    public TotemManager() {
        Util.EVENT_BUS.register(this);
        }

    @Subscribe
    public void onPacketInbound(PacketEvent event) {
        if (mc.world != null) {
            if (event.getPacket() instanceof EntityStatusS2CPacket packet
                    && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity entity = packet.getEntity(mc.world);
                if (entity != null && entity.isAlive()) {
                    totems.put(entity.getUuid(), totems.containsKey(entity.getUuid()) ?
                            totems.get(entity.getUuid()) + 1 : 1);
                }
            }
        }
    }





    public void onRemoveEntity(EntityDeathEvent event) {
        if (event.getEntity() == mc.player) {
            return;
        }
        totems.remove(event.getEntity().getUuid());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        totems.clear();
    }

    /**
     * Returns the number of totems popped by the given {@link PlayerEntity}
     *
     * @param entity
     * @return Ehe number of totems popped by the player
     */
    public int getTotems(Entity entity) {
        return totems.getOrDefault(entity.getUuid(), 0);
    }
    public long getLastPopTime(Entity entity)
    {
        return totems2.getOrDefault(entity.getUuid(), new TotemData(-1, 0)).getLastPopTime();
    }
    public static class TotemData
    {
        private final long lastPopTime;
        private final int pops;

        public TotemData(long lastPopTime, int pops)
        {
            this.lastPopTime = lastPopTime;
            this.pops = pops;
        }

        public int getPops()
        {
            return pops;
        }

        public long getLastPopTime()
        {
            return lastPopTime;
        }
    }
}
