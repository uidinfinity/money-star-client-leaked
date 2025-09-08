package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.render.CameraClipEvent;

public class ViewClip extends Module {

    public Setting<Float> distance = num("Distance", 1.0f, 3.5f, 20.0f);

    public ViewClip() {
        super("ViewClip", "Clips your third-person camera through blocks", Category.RENDER, true, false, false);
    }
    @Subscribe
    public void onCameraClip(CameraClipEvent event)
    {
        event.cancel();
        event.setDistance(distance.getValue());
    }

}
