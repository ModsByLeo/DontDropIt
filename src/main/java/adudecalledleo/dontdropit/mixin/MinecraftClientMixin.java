package adudecalledleo.dontdropit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Redirect(method = "handleInputEvents",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z",
                       ordinal = 7))
    public boolean disableDropKey(KeyBinding binding) {
        return false;
    }
}
