package me.money.star.event.impl.gui;

import me.money.star.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class RenderScreenEvent extends Event {
    public final MatrixStack matrixStack;

    public RenderScreenEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
