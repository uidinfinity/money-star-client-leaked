package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.event.impl.MouseUpdateEvent;
import me.money.star.event.impl.PerspectiveUpdateEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.camera.CameraRotationEvent;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import me.money.star.client.gui.modules.Module;



public final class FreeLook extends Module
{
    private float cameraYaw;
    private float cameraPitch;
    private Perspective perspective;


    public FreeLook()
    {
        super("FreeLook", "Allows you to freely move the camera in third person", Category.WORLD,true,false,false);
    }

    @Override
    public void onEnable()
    {
        perspective = mc.options.getPerspective();
    }

    @Override
    public void onDisable()
    {
        if (perspective != null)
        {
            mc.options.setPerspective(perspective);
        }
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (perspective != null && perspective != Perspective.THIRD_PERSON_BACK)
        {
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    @Subscribe
    public void onPerspectiveUpdate(PerspectiveUpdateEvent event)
    {
        if (mc.options.getPerspective() != event.getPerspective() && event.getPerspective() != Perspective.FIRST_PERSON)
        {
            cameraYaw = mc.player.getYaw();
            cameraPitch = mc.player.getPitch();
        }
    }

    @Subscribe
    public void onCameraRotation(CameraRotationEvent event)
    {
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON)
        {
            event.setYaw(cameraYaw);
            event.setPitch(cameraPitch);
        }
    }

    @Subscribe
    public void onMouseUpdate(MouseUpdateEvent event)
    {
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON)
        {
            event.cancel();
            changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
        }
    }

    /**
     * @param cursorDeltaX
     * @param cursorDeltaY
     * @see net.minecraft.entity.Entity#changeLookDirection(double, double)
     */
    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY)
    {
        float f = (float) cursorDeltaY * 0.15F;
        float g = (float) cursorDeltaX * 0.15F;
        this.cameraPitch += f;
        this.cameraYaw += g;
        this.cameraPitch = MathHelper.clamp(cameraPitch, -90.0F, 90.0F);
    }

}
