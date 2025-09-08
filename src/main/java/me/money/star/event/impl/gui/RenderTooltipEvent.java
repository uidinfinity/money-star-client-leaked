package me.money.star.event.impl.gui;

import me.money.star.event.Cancelable;
import me.money.star.event.StageEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class RenderTooltipEvent extends StageEvent {
    public final DrawContext context;
    private final ItemStack stack;
    //
    private final int x, y;

    public RenderTooltipEvent(DrawContext context, ItemStack stack, int x, int y) {
        this.context = context;
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public DrawContext getContext() {
        return context;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
