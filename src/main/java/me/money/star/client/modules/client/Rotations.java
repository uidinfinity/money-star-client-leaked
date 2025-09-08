package me.money.star.client.modules.client;

import me.money.star.client.gui.modules.ConcurrentModule;
import me.money.star.client.settings.Setting;

public class Rotations extends ConcurrentModule {
    private static Rotations INSTANCE;

    public Setting<Boolean> movementFix = bool("MovementFix", false);
    public Setting<Boolean> mouseSensFix = bool("MouseSensFix", false);
    public Setting<Float> preserveTicks = num("PreserveTicks ", 10.0f, 0.0f, 20.0f);

    private float prevYaw;
    public Rotations() {
        super("Rotations", "Rotation settings", Category.CLIENT, true, false, false);
        INSTANCE = this;
    }
    public static Rotations getInstance()
    {
        return INSTANCE;
    }


    public boolean getMovementFix()
    {
        return movementFix.getValue();
    }

    public boolean getMouseSensFix()
    {
        return mouseSensFix.getValue();
    }


    public float getPreserveTicks()
    {
        return preserveTicks.getValue();
    }
}
