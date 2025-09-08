package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.Render3DEvent;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.RenderUtil;
import me.money.star.util.traits.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class BlockHighlight extends Module {
    public enum BoxMode {
       Fill,Outline
    }
    public Setting<BoxMode> mode = mode("Mode", BoxMode.Outline);
    public Setting<Double> line = num("LineWidth", 2.0,0.0,5.0);

    public BlockHighlight() {
        super("BlockHighlight", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
    }

    @Subscribe public void onRender3D(Render3DEvent event) {
        Color color = new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);
        if (Util.mc.crosshairTarget instanceof BlockHitResult result) {
            VoxelShape shape = Util.mc.world.getBlockState(result.getBlockPos()).getOutlineShape(Util.mc.world, result.getBlockPos());
            if (shape.isEmpty()) return;
            Box box = shape.getBoundingBox();
            box = box.offset(result.getBlockPos());
            if(mode.getValue()== BoxMode.Outline) {
                RenderUtil.drawBox(event.getMatrix(), box, Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) :color, line.getValue());
            }
            if(mode.getValue()== BoxMode.Fill) {
                RenderUtil.drawBoxFilled(event.getMatrix(), box,Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) :new Color( Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(),75));
                RenderUtil.drawBox(event.getMatrix(), box,Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : color, line.getValue());
            }
        }
    }
}
