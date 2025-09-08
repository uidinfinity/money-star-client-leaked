package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.Render3DEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.LookDirectionEvent;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;

public class RotationLock extends Module {

    public Setting<Boolean> Pitch = bool("CustomPitch", false);

    public Setting<Float> pitch = num("Pitch", 90f, -180f, 180f);
    public RotationLock() {
        super("RotationLock", "Locks player yaw to a cardinal axis", Category.WORLD,true,false,false);
    }
    public Setting<Boolean> lock = bool("Lock", false);
    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE) {
            float yaw = Math.round(Util.mc.player.getYaw() / 45.0f) * 45.0f;
            Entity vehicle = Util.mc.player.getVehicle();
            if (vehicle != null) {
                vehicle.setYaw(yaw);
                if (vehicle instanceof LlamaEntity llama) {
                    llama.setHeadYaw(yaw);
                }
                return;
            }
            Util.mc.player.setYaw(yaw);
            Util.mc.player.setHeadYaw(yaw);
        }
    }

    @Subscribe
    public void onLookDirection(LookDirectionEvent event) {
        if (lock.getValue()) {
            event.cancel();
        }
    }
    public void onRender3D(Render3DEvent m) {
        if (Pitch.getValue()) {
            Util.mc.player.setPitch(pitch.getValue());
        }
    }
}
