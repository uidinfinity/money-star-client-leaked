package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.Render2DEvent;
import me.money.star.util.player.MovementUtil;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class CrossHair extends Module {

    public Setting<Float> width  = num("Width", 2.0f,0.0f,5.0f);
    public Setting<Float> height   = num("Height", 2.0f,0.0f,10.0f);
    public Setting<Float> gap  = num("Gap", 2.0f,0.0f,10.0f);
    public Setting<Boolean> dynamic  = bool("Dynamic", true);
    public Setting<Boolean> outline  = bool("Outline ", true);

    public CrossHair() {
        super("CrossHair", "Makes you a crosshair from CS:GO.", Category.RENDER, true, false, false);
    }

    @Subscribe public void onRender2D(Render2DEvent event) {
        if(fullNullCheck()) return;

        MatrixStack matrices = event.getMatrices();

        float x = mc.getWindow().getScaledWidth()/2f;
        float y = mc.getWindow().getScaledHeight()/2f;

        float w = width.getValue().floatValue()/2f;
        float h = height.getValue().floatValue();
        float g = gap.getValue().floatValue() + (moving() ? 2 : 0);

        RenderUtil.renderQuad(matrices, x - w, y - h - g, x + w, y - g,Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 200));
        RenderUtil.renderQuad(matrices, x + g, y - w, x + h + g, y + w,Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 200));
        RenderUtil.renderQuad(matrices, x - w, y + g, x + w, y + h + g, Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 200));
        RenderUtil.renderQuad(matrices, x - h - g, y - w, x - g, y + w, Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 200));

        if (outline.getValue()) {
            RenderUtil.renderOutline(matrices, x - w, y - h - g, x + w, y - g, Color.BLACK); // N
            RenderUtil.renderOutline(matrices, x + g, y - w, x + h + g, y + w, Color.BLACK); // E
            RenderUtil.renderOutline(matrices, x - w, y + g, x + w, y + h + g, Color.BLACK); // S
            RenderUtil.renderOutline(matrices, x - h - g, y - w, x - g, y + w, Color.BLACK); // W
        }
    }

    private boolean moving() {
        return (mc.player.isSneaking() || MovementUtil.isMoving()  || !mc.player.isOnGround()) && dynamic.getValue();
    }

}
