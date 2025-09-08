package me.money.star.client.gui.modules;

import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.manager.player.rotation.Rotation;

public class RotationModule extends Module {
    private final int rotationPriority;
    public RotationModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening)
    {
        super(name,description,category,hasListener,hidden,alwaysListening);
        this.rotationPriority = 100;
    }


    public RotationModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening, int rotationPriority)
    {
        super(name,description,category,hasListener,hidden,alwaysListening);

        this.rotationPriority = rotationPriority;
    }
    protected void setRotation(float yaw, float pitch)
    {
        MoneyStar.rotationManager.setRotation(new Rotation(getRotationPriority(), yaw, pitch));
    }

    protected void setRotationSilent(float yaw, float pitch) {
        MoneyStar.rotationManager.setRotationSilent(yaw, pitch);
    }

    /**
     * Sets client look yaw and pitch
     * @param yaw
     * @param pitch
     */
    protected void setRotationClient(float yaw, float pitch) {
        MoneyStar.rotationManager.setRotationClient(yaw, pitch);
    }

    protected boolean isRotationBlocked() {
        return MoneyStar.rotationManager.isRotationBlocked(getRotationPriority());
    }

    protected int getRotationPriority() {
        return rotationPriority;
    }
}
