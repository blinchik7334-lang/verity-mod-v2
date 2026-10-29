package com.example.verity.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
    @Invoker("hasLabel")
    boolean verity$hasLabel(Entity entity);

    @Invoker("renderLabelIfPresent")
    void verity$renderLabel(Entity entity, Text text, MatrixStack matrices,
                            VertexConsumerProvider consumers, int light);
}
