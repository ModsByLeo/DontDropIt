package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.config.ModConfigHolder;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity_DisableDropKey {
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void dontdropit$disableDropKey(boolean dropEntireStack, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfigHolder.getConfig().dropDelay.enabled)
            cir.setReturnValue(false);
    }
}
