package com.example.verity.mixin;

import com.example.verity.VerityClient;
import com.example.verity.client.VerityModels;
import com.example.verity.client.VerityRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true)
    private void verity$render(AbstractClientPlayerEntity player, float yaw, float tickDelta,
                               MatrixStack matrices, VertexConsumerProvider consumers, int light,
                               CallbackInfo ci) {
        String id = VerityClient.ACTIVE.get(player.getUuid());
        if (id == null) return;

        VerityModels.Form form = VerityModels.get(id);
        if (form == null) return; // нет 3D-модели — рисуем обычного игрока (со скином)

        VerityRenderer.render(form, player, tickDelta, matrices, consumers, light);

        // Ник над головой
        EntityRendererAccessor acc = (EntityRendererAccessor) (Object) this;
        if (acc.verity$hasLabel(player)) {
            acc.verity$renderLabel(player, player.getDisplayName(), matrices, consumers, light);
        }

        ci.cancel();
    }
}
