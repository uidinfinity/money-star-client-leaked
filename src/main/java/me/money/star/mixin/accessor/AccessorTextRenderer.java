package me.money.star.mixin.accessor;

import net.minecraft.client.font.FontStorage;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Shoreline
 * @since 1.0
 */
@Mixin(TextRenderer.class)
public interface AccessorTextRenderer {
    /**
     * @return
     */
    @Accessor("validateAdvance")
    boolean hookGetValidateAdvance();

    /**
     * @param id
     * @return
     */
    @Invoker("getFontStorage")
    FontStorage hookGetFontStorage(Identifier id);

    /**
     * @param glyphRenderer
     * @param bold
     * @param italic
     * @param weight
     * @param x
     * @param y
     * @param matrix
     * @param vertexConsumer
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @param light
     */

}
