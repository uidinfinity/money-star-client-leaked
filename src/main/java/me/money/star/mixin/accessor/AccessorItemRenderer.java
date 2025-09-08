package me.money.star.mixin.accessor;

import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.minecraft.client.render.VertexConsumer;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface AccessorItemRenderer {

}
