package com.example.verity.mixin;

import com.example.verity.VerityClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void verity$swapSkin(CallbackInfoReturnable<Identifier> cir) {
        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
        if (VerityClient.ACTIVE.containsKey(self.getUuid())) {
            cir.setReturnValue(VerityClient.TEXTURE);
        }
    }
}
