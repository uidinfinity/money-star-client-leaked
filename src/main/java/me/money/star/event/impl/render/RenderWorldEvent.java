package me.money.star.event.impl.render;

import me.money.star.event.Event;
import net.minecraft.client.util.math.MatrixStack;

/**
 * @author linus
 * @since 1.0
 */
public class RenderWorldEvent extends Event {
    //
    private final MatrixStack matrices;
    private final float tickDelta;

    /**
     * @param matrices
     */
    public RenderWorldEvent(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    /**
     * @return
     */
    public MatrixStack getMatrices() {
        return matrices;
    }

    /**
     * @return
     */
    public float getTickDelta() {
        return tickDelta;
    }

    public static class Game extends RenderWorldEvent {

        /**
         * @param matrices
         * @param tickDelta
         */
        public Game(MatrixStack matrices, float tickDelta) {
            super(matrices, tickDelta);
        }
    }
}
