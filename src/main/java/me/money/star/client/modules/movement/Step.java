package me.money.star.client.modules.movement;

import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.util.traits.Util;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
    private final Setting<Float> height = num("Height", 2f, 1f, 3f);

    public Step() {
        super("Step", "step..", Category.MOVEMENT, true, false, false);
    }

    private float prev;

    @Override
    public void onEnable() {
        if (nullCheck()) {
            prev = 0.6f;
            return;
        }
        prev = Util.mc.player.getStepHeight();
    }

    @Override public void onDisable() {
        if (nullCheck()) return;
        Util.mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(prev);
    }

    @Override public void onUpdate() {
        if (nullCheck()) return;
        Util.mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(height.getValue());
    }
}
