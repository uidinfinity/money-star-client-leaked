package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.passive.EntitySteerEvent;
import me.money.star.event.impl.network.MountJumpStrengthEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;

public class EntityControl extends Module {



    public Setting<Float> jumpStrength = num("JumpStrength ", 0.7f, 0.1f, 2.0f);


    public EntityControl() {
        super("EntityControl", "Allows you to steer entities without a saddle",
                Category.MOVEMENT,true,false,false);
    }

    @Subscribe
    public void onTick(TickEvent event) {
        Entity vehicle = Util.mc.player.getVehicle();
        if (vehicle == null) {
            return;
        }
        vehicle.setYaw(Util.mc.player.getYaw());
        if (vehicle instanceof LlamaEntity llama) {
            llama.headYaw = Util.mc.player.getYaw();
        }
    }

    @Subscribe
    public void onEntitySteer(EntitySteerEvent event) {
        event.cancel();
    }

    @Subscribe
    public void onMountJumpStrength(MountJumpStrengthEvent event) {
        event.cancel();
        event.setJumpStrength(jumpStrength.getValue());
    }
}
