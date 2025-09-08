package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.network.InteractType;
import me.money.star.util.traits.IPlayerInteractEntityC2SPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.util.Hand;


public class AutoMount extends Module {

    public Setting<Float> range = num("Range", 4.0f, 0.1f, 6.0f);
    public Setting<Boolean> force = bool("ForceMount", false);
    public Setting<Boolean> horse = bool("Horse", true);
    public Setting<Boolean>  donkey = bool("Donkey", true);
    public Setting<Boolean>  mule = bool("Mule", true);
    public Setting<Boolean>  llama = bool("Llama", true);


    public AutoMount()
    {
        super("AutoMount", "Mounts nearby entities", Category.WORLD,true,false,false);
    }

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (mc.player.isRiding())
        {
            return;
        }
        for (Entity entity : mc.world.getEntities())
        {
            double dist = mc.player.getEyePos().distanceTo(entity.getPos());
            if (dist > range.getValue())
            {
                continue;
            }
            if (checkMount(entity))
            {
                mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);
                return;
            }
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet && force.getValue()
                && packet.getType() == InteractType.INTERACT_AT && checkMount(packet.getEntity()))
        {
            event.cancel();
        }
    }

    private boolean checkMount(Entity entity)
    {
        return horse.getValue() && (entity instanceof HorseEntity horseEntity && !horseEntity.isBaby() || entity instanceof SkeletonHorseEntity)
                || donkey.getValue() && entity instanceof DonkeyEntity || mule.getValue() && entity instanceof MuleEntity
                || llama.getValue() && entity instanceof LlamaEntity llamaEntity && !llamaEntity.isBaby();
    }
}
