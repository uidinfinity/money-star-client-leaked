package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.TickEvent;
import me.money.star.mixin.accessor.AccessorMinecraftClient;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.traits.IMinecraftClient;
import me.money.star.util.traits.Util;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;


public class Trigger extends Module {

    //
    public Setting<TriggerMode> mode = mode("Mode", TriggerMode.Normal);

    public Setting<Float> attackSpeed = num("Attack-Speed", 8.0f, 0.1f, 20.0f);
    public Setting<Float> randomSpeed = num("Random-Speed", 2.0f, 0.1f, 10.0f);

    //
    private final Timer triggerTimer = new CacheTimer();


    public Trigger() {
        super("Trigger", "Automatically attacks entities in the crosshair",
                Category.COMBAT,true,false,false);
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() != Stage.PRE) {
            return;
        }
        boolean buttonDown = switch (mode.getValue()) {
            case Normal -> {
                if (Util.mc.crosshairTarget == null || Util.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
                    yield false;
                }
                EntityHitResult entityHit = (EntityHitResult) Util.mc.crosshairTarget;
                final Entity crosshairEntity = entityHit.getEntity();
                if (Util.mc.player.isTeammate(crosshairEntity)
                        || crosshairEntity.getDisplayName() != null && MoneyStar.friendManager.isFriend(crosshairEntity.getName().getString())) {
                    yield false;
                }
                yield true;
            }
            case Always -> true;
        };
        double d = Math.random() * randomSpeed.getValue() * 2.0 - randomSpeed.getValue();
        if (buttonDown && triggerTimer.passed(1000.0 - Math.max(attackSpeed.getValue() + d, 0.5) * 50.0)) {
            ((IMinecraftClient) Util.mc).leftClick();
            ((AccessorMinecraftClient) Util.mc).hookSetAttackCooldown(0);
            triggerTimer.reset();
        }
    }

    public enum TriggerMode {
        Normal,
        Always
    }
}
